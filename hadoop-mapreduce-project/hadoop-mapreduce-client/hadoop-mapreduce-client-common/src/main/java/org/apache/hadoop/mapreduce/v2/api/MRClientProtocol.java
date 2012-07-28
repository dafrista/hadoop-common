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

package org.apache.hadoop.mapreduce.v2.api;

import org.apache.hadoop.mapreduce.v2.api.protocolrecords.FailTaskAttemptRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.FailTaskAttemptResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetCountersRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetCountersResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetDelegationTokenRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetDelegationTokenResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetDiagnosticsRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetDiagnosticsResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetJobReportRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetJobReportResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskAttemptCompletionEventsRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskAttemptCompletionEventsResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskAttemptReportRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskAttemptReportResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskReportRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskReportResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskReportsRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.GetTaskReportsResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillJobRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillJobResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillTaskAttemptRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillTaskAttemptResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillTaskRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.KillTaskResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.PartialCommitJobRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.PartialCommitJobResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.ResumeTaskRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.ResumeTaskResponse;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.SuspendTaskAttemptRequest;
import org.apache.hadoop.mapreduce.v2.api.protocolrecords.SuspendTaskAttemptResponse;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;

public interface MRClientProtocol {
  public GetJobReportResponse getJobReport(GetJobReportRequest request) throws YarnRemoteException;
  public GetTaskReportResponse getTaskReport(GetTaskReportRequest request) throws YarnRemoteException;
  public GetTaskAttemptReportResponse getTaskAttemptReport(GetTaskAttemptReportRequest request) throws YarnRemoteException;
  public GetCountersResponse getCounters(GetCountersRequest request) throws YarnRemoteException;
  public GetTaskAttemptCompletionEventsResponse getTaskAttemptCompletionEvents(GetTaskAttemptCompletionEventsRequest request) throws YarnRemoteException;
  public GetTaskReportsResponse getTaskReports(GetTaskReportsRequest request) throws YarnRemoteException;
  public GetDiagnosticsResponse getDiagnostics(GetDiagnosticsRequest request) throws YarnRemoteException;
  public KillJobResponse killJob(KillJobRequest request) throws YarnRemoteException;
  public PartialCommitJobResponse partialCommitJob(PartialCommitJobRequest request) throws YarnRemoteException;
  public KillTaskResponse killTask(KillTaskRequest request) throws YarnRemoteException;
  public KillTaskAttemptResponse killTaskAttempt(KillTaskAttemptRequest request) throws YarnRemoteException;
  public FailTaskAttemptResponse failTaskAttempt(FailTaskAttemptRequest request) throws YarnRemoteException;
  public SuspendTaskAttemptResponse suspendTaskAttempt(SuspendTaskAttemptRequest request) throws YarnRemoteException;
  public ResumeTaskResponse resumeTask(ResumeTaskRequest request) throws YarnRemoteException;
  public GetDelegationTokenResponse getDelegationToken(GetDelegationTokenRequest request) throws YarnRemoteException;
}
