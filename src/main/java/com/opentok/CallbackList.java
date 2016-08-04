/**
 * OpenTok Java SDK
 * Copyright (C) 2016 TokBox, Inc.
 * http://www.tokbox.com
 *
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Represents an list of archives of OpenTok session(s).
 */
@JsonFormat(shape=JsonFormat.Shape.ARRAY)
public class CallbackList extends ArrayList<Callback> {

}
