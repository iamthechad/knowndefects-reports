/*******************************************************************************
 * Copyright (c) 2011-2013 Megatome Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/****************************************************************
 * Code in this class borrowed and adapted from the Scannotation library
 * available from http://scannotation.sourceforge.net/
 ****************************************************************/
package com.megatome.knowndefects.scan;

import java.io.InputStream;

/**
 * Interface for an iterator that provides an InputStream
 * Code in this class borrowed and adapted from the Scannotation library
 * available from <a href="http://scannotation.sourceforge.net/">http://scannotation.sourceforge.net/</a>
 */
public interface StreamIterator {
    public InputStream next() throws AnnotationScanException;
}
