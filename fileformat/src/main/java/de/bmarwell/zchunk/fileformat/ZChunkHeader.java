/*
 * Copyright 2019, the zchunk-java contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bmarwell.zchunk.fileformat;

import org.immutables.value.Value;

/**
 * The header consists of four parts:
 *
 * <ul>
 * <li>The lead: Everything necessary to validate the header</li>
 * <li>The preface: Metadata about the zchunk file</li>
 * <li>The index: Details about each chunk</li>
 * <li>The signatures: Signatures used to sign the zchunk file</li>
 * </ul>
 */
@Value.Immutable
public interface ZChunkHeader {

  ZChunkHeaderLead getLead();

  ZChunkHeaderPreface getPreface();

  ZChunkHeaderIndex getIndex();

  ZChunkHeaderSignatures getSignatures();

}
