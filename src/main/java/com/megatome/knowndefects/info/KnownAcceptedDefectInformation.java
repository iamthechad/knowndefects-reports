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

import com.megatome.knowndefects.Constants;

/**
 * Information class representing KnownAndAcceptedDefect annotation
 * @see AnnotationInformation
 */
public class KnownAcceptedDefectInformation extends AbstractInformation {
    /**
     * Return the author value
     * @return Author
     */
    public String getAuthor() {
        return getMethodValue("author");
    }

    /**
     * Return the date value
     * @return Date string
     */
    public String getDate() {
        return getMethodValue("date");
    }

    /**
     * Return the reason value
     * @return Reason
     */
    public String getReason() {
        return getMethodValue("reason");
    }

    @Override
    public String getAnnotationName() {
        return Constants.KNOWN_ACCEPTED_DEFECT_ANNOTATION_CLASS;
    }
}
