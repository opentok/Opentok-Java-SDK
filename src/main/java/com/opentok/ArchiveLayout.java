/**
 * OpenTok Java SDK
 * Copyright (C) 2019 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *  Represents a <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">layout
 *  configuration</a> for a composed archive.
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public class ArchiveLayout {
    private Type type;
    private String stylesheet;

    /**
     * Do not call the <code>ArchiveLayout()</code> constructor. To set the layout of an
     * archive, call the {@link OpenTok#setArchiveLayout(String archiveId, ArchiveProperties properties)} method.
     */
   public ArchiveLayout(Type type, String stylesheet) {
        this.type = type;
        this.stylesheet = stylesheet;
    }

    /**
     * Do not call the <code>ArchiveLayout()</code> constructor. To set the layout of an
     * archive, call the {@link OpenTok#setArchiveLayout(String archiveId, ArchiveProperties properties)} method. See 
     */
    public ArchiveLayout(Type type) {
        this.type = type;
    }

    /**
     * Enumerates <code>type</code> values for the layout.
     */
    public enum Type {
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
        HORIZONTAL("horizontalPresentation"),
        /**
         * Represents a custom layout type.
         */
        CUSTOM("custom");

        private String serialized;

        private Type(String s) {
            serialized = s;
        }

        @JsonValue
        public String toString() {
            return serialized;
        }
    }

    /**
     * Returns the layout type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the layout type.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the stylesheet for a custom layout.
     */
    public String getStylesheet() {
        return stylesheet;
    }

    /**
     * Sets the stylesheet for a custom layout.
     */
    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

}
