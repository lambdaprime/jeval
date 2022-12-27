/*
 * Copyright 2019 lambdaprime
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Why jeval startup is slow?
 *
 * <p>jeval is based on LocalExecutionControl execution engine which belongs to jshell. Possibly it
 * is the source of slow startup but it was not verified.
 *
 * <p>LocalExecutionControl vs JdiExecutionControl
 *
 * <p>LocalExecutionControl starts inside same JVM which means that you cannot control JVM
 * parameters within jeval once it is started (ex -Duser.dir). JdiExecutionControl runs in separate
 * JVM which can be controlled from jeval JVM. Unfortunately evaluating System.exit from it causes
 * exception being printed to stderr within jshell API. Which would mean that users will see it each
 * time they try exit the script.
 */
package id.jeval;
