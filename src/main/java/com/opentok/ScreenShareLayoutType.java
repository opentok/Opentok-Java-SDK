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

        private ScreenShareLayoutType(String s) {
            serialized = s;
        }

        @JsonValue
        public String toString() {
            return serialized;
        }

}
