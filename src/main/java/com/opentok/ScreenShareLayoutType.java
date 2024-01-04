/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * 
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumerates <code>type</code> values for the layout.
 */
public enum ScreenShareLayoutType {

        /**
         * Represents the picture-in-picture (pip) layout type.
         */
        PIP("pip"),
        /**
         * Represents the picture-in-picture (pip) layout type.
         */
        BESTFIT("bestFit"),
        /**
         * Represents the vertical presentation layout type.
         */
        VERTICAL("verticalPresentation"),
        /**
         * Represents the horizontal presentation layout type.
         */
        HORIZONTAL("horizontalPresentation");

        private String serialized;

        ScreenShareLayoutType(String s) {
            serialized = s;
        }

        @JsonValue
        public String toString() {
            return serialized;
        }

}
