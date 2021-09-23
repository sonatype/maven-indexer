<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
Indexer Core Notes
==================

MINDEXER-2 related Index core changes
-------------------------------------

New locking semantics introduced to be able to cope with multithreaded processing. This mostly affects server-like apps integrating the indexer, not as much IDEs.

IndexContext new methods:

* commit/rollback -- for commiting changes, but also reopening readers/searchers if appropriate.
* lock/unlock -- to perform shared locking, guaranteeing no reader/searcher reopen will occur.

IteratorSearchResult/IteratorResultSet new methods:

* both become Closeable. If you do NOT consume all of iterator (when automatic cleanup happens), you have to explicitly call result.close() to release locks!

Others:

* warmUp of readers/searchers added, currently a naive implementation, later we can refine it
* smaller fixes


Notes: if an app integrating Indexer Core did NOT tamper with indexingContexts directly (by using one of it's ctx.getIndexWriter(),
ctx.getIndexReader() and ctx.getIndexSearcher()), it should almost not notice anything. The only change needed is to explicitly close
non-consumed IteratorSearchResults. Otherwise, you have to adapt and manually fiddle with locking of contexts, or use proper API calls. In future, these
methods and direct IndexingContext use within integrating applications will be highly discouraged!