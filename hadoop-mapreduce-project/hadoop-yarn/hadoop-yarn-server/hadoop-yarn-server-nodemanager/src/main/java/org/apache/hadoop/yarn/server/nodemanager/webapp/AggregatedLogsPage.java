/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.nodemanager.webapp;

import static org.apache.hadoop.yarn.server.nodemanager.webapp.NMWebParams.CONTAINER_ID;
import static org.apache.hadoop.yarn.server.nodemanager.webapp.NMWebParams.ENTITY_STRING;
import static org.apache.hadoop.yarn.util.StringHelper.join;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.ACCORDION;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.ACCORDION_ID;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.THEMESWITCHER_ID;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.initID;


import org.apache.hadoop.yarn.webapp.SubView;


public class AggregatedLogsPage extends NMView {

  /* (non-Javadoc)
   * @see org.apache.hadoop.yarn.server.nodemanager.webapp.NMView#preHead(org.apache.hadoop.yarn.webapp.hamlet.Hamlet.HTML)
   */
  @Override
  protected void preHead(Page.HTML<_> html) {
    String logEntity = $(ENTITY_STRING);
    if (logEntity == null || logEntity.isEmpty()) {
      logEntity = $(CONTAINER_ID);
    }
    if (logEntity == null || logEntity.isEmpty()) {
      logEntity = "UNKNOWN";
    }
    set(TITLE, join("Logs for ", logEntity));
    set(ACCORDION_ID, "nav");
    set(initID(ACCORDION, "nav"), "{autoHeight:false, active:0}");
    set(THEMESWITCHER_ID, "themeswitcher");
  }

  @Override
  protected Class<? extends SubView> content() {
    return AggregatedLogsBlock.class;
  }
  
  @Override
  protected Class<? extends SubView> nav() {
    return AggregatedLogsNavBlock.class;
  }
}
