/**
 * OpenTok Java SDK
 * Copyright (C) 2018 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 *  Represents a <a href="https://tokbox.com/developer/guides/archiving/layout-control.html">layout configuration</a>
 *  for a composed archive
 */
@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public class ArchiveLayout {
    private Type type;
    private String stylesheet;

    public ArchiveLayout(Type type, String stylesheet) {
        this.type = type;
        this.stylesheet = stylesheet;
    }

    public ArchiveLayout(Type type) {
        this.type = type;
    }

    public enum Type {
        PIP("pip"),
        BESTFIT("bestFit"),
        VERTICAL("verticalPresentation"),
        HORIZONTAL("horizontalPresentation"),
        CUSTOM("custom");

        private String serialized;

        private Type(String s) {
            serialized = s;
        }

        @JsonValue
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getStylesheet() {
        return stylesheet;
    }

    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

}
