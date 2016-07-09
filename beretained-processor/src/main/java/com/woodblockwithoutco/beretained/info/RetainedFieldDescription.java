/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package com.woodblockwithoutco.beretained.info;

import javax.lang.model.type.TypeMirror;

/**
 * Description of retained field.
 */
public class RetainedFieldDescription {
    /**
     * Field type
     */
    public final TypeMirror type;

    /**
     * Name of field.
     */
    public final String name;

    /**
     * Allow null - if false, null check will be added.
     */
    public final boolean nullAllowed;

    public RetainedFieldDescription(TypeMirror type, String name, boolean nullAllowed) {
        this.type = type;
        this.name = name;
        this.nullAllowed = nullAllowed;
    }
}
