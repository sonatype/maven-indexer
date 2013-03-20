package org.apache.maven.index.creator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kperikov
 * Date: 20.03.13
 * Time: 10:55 *
 */
@Component(role = IndexCreator.class, hint = DependenciesIndexCreator.ID)
public class DependenciesIndexCreator extends AbstractIndexCreator
        implements LegacyDocumentUpdater {

    public static final String ID = "dependecies";

    public DependenciesIndexCreator() {
        super(ID);
    }

    public static final IndexerField FLD_DEPENDENCIES = new IndexerField(MAVEN.DEPENDENCIES, IndexerFieldVersion.V3,
            "dependencies", "Dependencies", Field.Store.YES, Field.Index.ANALYZED);

    public static final IndexerField FLD_GROUP_ID = new IndexerField(MAVEN.GROUP_ID, IndexerFieldVersion.V3,
            "groupId", "GroupID", Field.Store.NO, Field.Index.ANALYZED);

    public static final IndexerField FLD_ARTIFACT_ID = new IndexerField(MAVEN.ARTIFACT_ID, IndexerFieldVersion.V3,
            "artifactId", "ArtifactID", Field.Store.NO, Field.Index.ANALYZED);

    public static final IndexerField FLD_VERSION = new IndexerField(MAVEN.VERSION, IndexerFieldVersion.V3,
            "version", "Version", Field.Store.NO, Field.Index.ANALYZED);

    public Collection<IndexerField> getIndexerFields() {
        return Arrays.asList(FLD_GROUP_ID, FLD_ARTIFACT_ID, FLD_VERSION, FLD_DEPENDENCIES);
    }

    public void populateArtifactInfo(ArtifactContext artifactContext) throws IOException {
        File artifact = artifactContext.getArtifact();
        File pom = artifactContext.getPom();
        ArtifactInfo ai = artifactContext.getArtifactInfo();
        if (pom != null) {
            ai.lastModified = pom.lastModified();
            ai.fextension = "pom";
        }
        // TODO handle artifacts without poms
//        if (pom != null) {
//            if (ai.classifier != null) {
//                ai.sourcesExists = ArtifactAvailablility.NOT_AVAILABLE;
//                ai.javadocExists = ArtifactAvailablility.NOT_AVAILABLE;
//            } else {
//                File sources = sl.locate(pom);
//                if (!sources.exists()) {
//                    ai.sourcesExists = ArtifactAvailablility.NOT_PRESENT;
//                } else {
//                    ai.sourcesExists = ArtifactAvailablility.PRESENT;
//                }
//
//                File javadoc = jl.locate(pom);
//                if (!javadoc.exists()) {
//                    ai.javadocExists = ArtifactAvailablility.NOT_PRESENT;
//                } else {
//                    ai.javadocExists = ArtifactAvailablility.PRESENT;
//                }
//            }
//        }

        ai.dependencies = "";

        Model model = artifactContext.getPomModel();
        if (model != null) {
            ai.name = model.getName();
            ai.description = model.getDescription();

            final List<Dependency> dependencies = model.getDependencies();
            ai.dependencies = dependencies.toArray().toString();

            // for main artifacts (without classifier) only:
            if (ai.classifier == null) {
                // only when this is not a classified artifact
                if (model.getPackaging() != null) {
                    // set the read value that is coming from POM
                    ai.packaging = model.getPackaging();
                } else {
                    // default it, since POM is present, is read, but does not contain explicit packaging
                    // TODO: this change breaks junit tests, but not sure why is "null" expected value?
                    // ai.packaging = "jar";
                }
            }
        }


//        if ("pom".equals(ai.packaging)) {
//            // special case, the POM _is_ the artifact
//            artifact = pom;
//        }

//        if (artifact != null) {
//            File signature = sigl.locate(artifact);
//            ai.signatureExists = signature.exists() ? ArtifactAvailablility.PRESENT : ArtifactAvailablility.NOT_PRESENT;
//            File sha1 = sha1l.locate(artifact);
//            if (sha1.exists()) {
//                try {
//                    ai.sha1 = StringUtils.chomp(FileUtils.fileRead(sha1)).trim().split(" ")[0];
//                } catch (IOException e) {
//                    artifactContext.addError(e);
//                }
//            }
//            ai.lastModified = artifact.lastModified();
//            ai.size = artifact.length();
//            ai.fextension = getExtension(artifact, artifactContext.getGav());
//            if (ai.packaging == null) {
//                ai.packaging = ai.fextension;
//            }
//        }
    }

    public void updateDocument(ArtifactInfo artifactInfo, Document document) {
        String info =
                new StringBuilder().append(artifactInfo.packaging).append(ArtifactInfo.FS).append(
                        Long.toString(artifactInfo.lastModified)).append(ArtifactInfo.FS).append(Long.toString(artifactInfo.size)).append(
                        ArtifactInfo.FS).append(artifactInfo.sourcesExists.toString()).append(ArtifactInfo.FS).append(
                        artifactInfo.javadocExists.toString()).append(ArtifactInfo.FS).append(artifactInfo.signatureExists.toString()).append(
                        ArtifactInfo.FS).append(artifactInfo.fextension).toString();
//        document.add(FLD_INFO.toField(info));
//        document.add(FLD_GROUP_ID_KW.toField(artifactInfo.groupId));
//        document.add(FLD_ARTIFACT_ID_KW.toField(artifactInfo.artifactId));
//        document.add(FLD_VERSION_KW.toField(artifactInfo.version));
        // V3
        document.add(FLD_GROUP_ID.toField(artifactInfo.groupId));
        document.add(FLD_ARTIFACT_ID.toField(artifactInfo.artifactId));
        document.add(FLD_VERSION.toField(artifactInfo.version));
        document.add(FLD_DEPENDENCIES.toField(artifactInfo.dependencies));
//        if (artifactInfo.name != null) {
//            document.add(FLD_NAME.toField(artifactInfo.name));
//        }
//        if (artifactInfo.description != null) {
//            document.add(FLD_DESCRIPTION.toField(artifactInfo.description));
//        }
//        if (artifactInfo.packaging != null) {
//            document.add(FLD_PACKAGING.toField(artifactInfo.packaging));
//        }
//        if (artifactInfo.classifier != null) {
//            document.add(FLD_CLASSIFIER.toField(artifactInfo.classifier));
//        }
//        if (artifactInfo.sha1 != null) {
//            document.add(FLD_SHA1.toField(artifactInfo.sha1));
//        }
    }

    public boolean updateArtifactInfo(Document document, ArtifactInfo artifactInfo) {
        boolean res = false;
        String uinfo = document.get(ArtifactInfo.UINFO);
        if (uinfo != null) {
            String[] r = ArtifactInfo.FS_PATTERN.split(uinfo);
            artifactInfo.groupId = r[0];
            artifactInfo.artifactId = r[1];
            artifactInfo.version = r[2];
            if (r.length > 3) {
                artifactInfo.classifier = ArtifactInfo.renvl(r[3]);
            }
            res = true;
        }
        String info = document.get(ArtifactInfo.INFO);
        if (info != null) {
            String[] r = ArtifactInfo.FS_PATTERN.split(info);
            artifactInfo.packaging = r[0];
            artifactInfo.lastModified = Long.parseLong(r[1]);
            artifactInfo.size = Long.parseLong(r[2]);
            artifactInfo.sourcesExists = ArtifactAvailablility.fromString(r[3]);
            artifactInfo.javadocExists = ArtifactAvailablility.fromString(r[4]);
            artifactInfo.signatureExists = ArtifactAvailablility.fromString(r[5]);
            if (r.length > 6) {
                artifactInfo.fextension = r[6];
            } else {
                if (artifactInfo.classifier != null //
                        || "pom".equals(artifactInfo.packaging) //
                        || "war".equals(artifactInfo.packaging) //
                        || "ear".equals(artifactInfo.packaging)) {
                    artifactInfo.fextension = artifactInfo.packaging;
                } else {
                    artifactInfo.fextension = "jar"; // best guess
                }
            }
            res = true;
        }

        String name = document.get(ArtifactInfo.NAME);
        if (name != null) {
            artifactInfo.name = name;
            res = true;
        }
        String description = document.get(ArtifactInfo.DESCRIPTION);
        if (description != null) {
            artifactInfo.description = description;

            res = true;
        }
        // sometimes there's a pom without packaging(default to jar), but no artifact, then the value will be a "null"
        // String
        if ("null".equals(artifactInfo.packaging)) {
            artifactInfo.packaging = null;
        }
        String sha1 = document.get(ArtifactInfo.SHA1);
        if (sha1 != null) {
            artifactInfo.sha1 = sha1;
        }
        return res;
    }

    public void updateLegacyDocument(ArtifactInfo ai, Document doc) {
        updateDocument(ai, doc);
        // legacy!
        if (ai.prefix != null) {
            doc.add(new Field(ArtifactInfo.PLUGIN_PREFIX, ai.prefix, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        if (ai.goals != null) {
            doc.add(new Field(ArtifactInfo.PLUGIN_GOALS, ArtifactInfo.lst2str(ai.goals), Field.Store.YES,
                    Field.Index.NO));
        }
        doc.removeField(ArtifactInfo.GROUP_ID);
        doc.add(new Field(ArtifactInfo.GROUP_ID, ai.groupId, Field.Store.NO, Field.Index.NOT_ANALYZED));
    }
}
