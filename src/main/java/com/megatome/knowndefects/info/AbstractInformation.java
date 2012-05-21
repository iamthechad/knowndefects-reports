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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of AnnotationInformation. Handles common behavior.
 * @see AnnotationInformation
 */
public abstract class AbstractInformation implements AnnotationInformation {
    private String className;
    private String methodName;
    private int lineNumber;

    private final Map<String, String> methods = new HashMap<String, String>();

    public List<String> getMethodNames() {
        return new ArrayList<String>(methods.keySet());
    }

    public String getMethodValue(String methodName) {
        return methods.get(methodName);
    }

    public void setMethodValue(String methodName, String methodValue) {
        methods.put(methodName, methodValue);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public int compareTo(AnnotationInformation information) {
        return this.getMethodName().compareTo(information.getMethodName());
    }
}
