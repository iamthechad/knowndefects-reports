/*******************************************************************************
 * Copyright (c) 2011 Megatome Technologies
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

package com.megatome.knowndefects.info;

import java.util.List;

/**
 * Interface representing generic annotation information found by the scanner.
 */
public interface AnnotationInformation {
    /**
     * Get the list of all found method names within the annotation.
     * @return Found annotation method names. Will be empty if none found.
     */
    public List<String> getMethodNames();

    /**
     * Get the value for a particular method name.
     * @param methodName Method name to find value for.
     * @return Value associated with the method name. May be null if methodName does not exist.
     */
    public String getMethodValue(String methodName);

    /**
     * Set the value for a specified method name.
     * @param methodName Method name, i.e. "author"
     * @param methodValue Method value, i.e. "cjohnston"
     */
    public void setMethodValue(String methodName, String methodValue);

    /**
     * Get the fully qualified class name of the object this annotation was found in.
     * @return Fully qualified class name.
     */
    public String getClassName();

    /**
     * Set the fully qualified class name of the object this annotation was found in.
     * @param className Class name
     */
    public void setClassName(String className);

    /**
     * Get the name of the method this annotation is attached to.
     * @return Method name
     */
    public String getMethodName();

    /**
     * Set the name of the method this annotation is attached to.
     * @param methodName Method name
     */
    public void setMethodName(String methodName);

    /**
     * Set the line number where the annotation was found. This is currently not used.
     * @param lineNumber Line number
     */
    public void setLineNumber(int lineNumber);

    /**
     * Get the line number where the annotation was found. This is currently not used.
     * @return Line number
     */
    public int getLineNumber();
}
