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

package org.apache.hadoop.examples;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Dummy class for testing MR framefork. Sleeps for a defined period 
 * of time in mapper and reducer. Generates fake input for map / reduce 
 * jobs. Note that generated number of input pairs is in the order 
 * of <code>numMappers * mapSleepTime / 100</code>, so the job uses
 * some disk space.
 */
public class SuspendTest extends Configured implements Tool {
  public static String MAP_SLEEP_COUNT = "mapreduce.sleepjob.map.sleep.count";
  public static String REDUCE_SLEEP_COUNT = 
    "mapreduce.sleepjob.reduce.sleep.count";
  public static String MAP_SLEEP_TIME = "mapreduce.sleepjob.map.sleep.time";
  public static String REDUCE_SLEEP_TIME = 
    "mapreduce.sleepjob.reduce.sleep.time";

  public static class SleepJobPartitioner extends 
      Partitioner<IntWritable, NullWritable> {
    public int getPartition(IntWritable k, NullWritable v, int numPartitions) {
      return k.get() % numPartitions;
    }
  }
  
  public static class EmptySplit extends InputSplit implements Writable {
    public void write(DataOutput out) throws IOException { }
    public void readFields(DataInput in) throws IOException { }
    public long getLength() { return 0L; }
    public String[] getLocations() { return new String[0]; }
  }

  public static class SleepInputFormat 
      extends InputFormat<IntWritable,IntWritable> {
    
    public List<InputSplit> getSplits(JobContext jobContext) {
      List<InputSplit> ret = new ArrayList<InputSplit>();
      int numSplits = jobContext.getConfiguration().
                        getInt(MRJobConfig.NUM_MAPS, 1);
      for (int i = 0; i < numSplits; ++i) {
        ret.add(new EmptySplit());
      }
      return ret;
    }
    
    public RecordReader<IntWritable,IntWritable> createRecordReader(
        InputSplit ignored, TaskAttemptContext taskContext)
        throws IOException {
      Configuration conf = taskContext.getConfiguration();
      final int count = conf.getInt(MAP_SLEEP_COUNT, 1);
      if (count < 0) throw new IOException("Invalid map count: " + count);
      final int redcount = conf.getInt(REDUCE_SLEEP_COUNT, 1);
      if (redcount < 0)
        throw new IOException("Invalid reduce count: " + redcount);
      final int emitPerMapTask = (redcount * taskContext.getNumReduceTasks());
      
      return new RecordReader<IntWritable,IntWritable>() {
        private int records = 0;
        private int emitCount = 0;
        private IntWritable key = null;
        private IntWritable value = null;
        public void initialize(InputSplit split, TaskAttemptContext context) {
        }

        public boolean nextKeyValue()
            throws IOException {
          if (count == 0) {
            return false;
          }
          key = new IntWritable();
          key.set(emitCount);
          int emit = emitPerMapTask / count;
          if ((emitPerMapTask) % count > records) {
            ++emit;
          }
          emitCount += emit;
          value = new IntWritable();
          value.set(emit);
          return records++ < count;
        }
        public IntWritable getCurrentKey() { return key; }
        public IntWritable getCurrentValue() { return value; }
        public void close() throws IOException { }
        public float getProgress() throws IOException {
          return count == 0 ? 100 : records / ((float)count);
        }
      };
    }
  }

  public static class SleepMapper 
      extends Mapper<IntWritable, IntWritable, IntWritable, NullWritable> {
    private long mapSleepDuration = 100;
    private int mapSleepCount = 1;
    private int count = 0;

    protected void setup(Context context) 
      throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      this.mapSleepCount =
        conf.getInt(MAP_SLEEP_COUNT, mapSleepCount);
      this.mapSleepDuration = mapSleepCount == 0 ? 0 :
        conf.getLong(MAP_SLEEP_TIME , 100) / mapSleepCount;
    }

    public void map(IntWritable key, IntWritable value, Context context
               ) throws IOException, InterruptedException {
      //it is expected that every map processes mapSleepCount number of records. 
      try {
        context.setStatus("Sleeping... (" +
          (mapSleepDuration * (mapSleepCount - count)) + ") ms left");
        Thread.sleep(mapSleepDuration);
      }
      catch (InterruptedException ex) {
        throw (IOException)new IOException(
            "Interrupted while sleeping").initCause(ex);
      }
      ++count;
      // output reduceSleepCount * numReduce number of random values, so that
      // each reducer will get reduceSleepCount number of keys.
      int k = key.get();
      for (int i = 0; i < value.get(); ++i) {
        context.write(new IntWritable(k + i), NullWritable.get());
      }
    }
  }
  
  public static class SleepReducer  
      extends Reducer<IntWritable, NullWritable, NullWritable, NullWritable> {
    private long reduceSleepDuration = 100;
    private int reduceSleepCount = 1;
    private int count = 0;

    protected void setup(Context context) 
      throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      this.reduceSleepCount =
        conf.getInt(REDUCE_SLEEP_COUNT, reduceSleepCount);
      this.reduceSleepDuration = reduceSleepCount == 0 ? 0 : 
        conf.getLong(REDUCE_SLEEP_TIME , 100) / reduceSleepCount;
    }

    public void reduce(IntWritable key, Iterable<NullWritable> values,
                       Context context)
      throws IOException {
      try {
        context.setStatus("Sleeping... (" +
            (reduceSleepDuration * (reduceSleepCount - count)) + ") ms left");
        Thread.sleep(reduceSleepDuration);
      
      }
      catch (InterruptedException ex) {
        throw (IOException)new IOException(
          "Interrupted while sleeping").initCause(ex);
      }
      count++;
    }
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new SuspendTest(), args);
    System.exit(res);
  }

  public Job createJob(int numMapper, int numReducer, 
                       long mapSleepTime, int mapSleepCount, 
                       long reduceSleepTime, int reduceSleepCount) 
      throws IOException {
    Configuration conf = getConf();
    conf.setLong(MAP_SLEEP_TIME, mapSleepTime);
    conf.setLong(REDUCE_SLEEP_TIME, reduceSleepTime);
    conf.setInt(MAP_SLEEP_COUNT, mapSleepCount);
    conf.setInt(REDUCE_SLEEP_COUNT, reduceSleepCount);
    conf.setInt(MRJobConfig.NUM_MAPS, numMapper);
    Job job = Job.getInstance(conf, "sleep");
    job.setNumReduceTasks(numReducer);
    job.setJarByClass(SuspendTest.class);
    job.setNumReduceTasks(numReducer);
    job.setMapperClass(SleepMapper.class);
    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(NullWritable.class);
    job.setReducerClass(SleepReducer.class);
    job.setOutputFormatClass(NullOutputFormat.class);
    job.setInputFormatClass(SleepInputFormat.class);
    job.setPartitionerClass(SleepJobPartitioner.class);
    job.setSpeculativeExecution(false);
    job.setJobName("Sleep job");
    FileInputFormat.addInputPath(job, new Path("ignored"));
    return job;
  }

  public int run(String[] args) throws Exception {

    if(args.length < 1) {
      System.err.println("SleepJob [-m numMapper] [-r numReducer]" +
          " [-mt mapSleepTime (msec)] [-rt reduceSleepTime (msec)]" +
          " [-recordt recordSleepTime (msec)]");
      ToolRunner.printGenericCommandUsage(System.err);
      return 2;
    }

    int numMapper = 1, numReducer = 1;
    long mapSleepTime = 100, reduceSleepTime = 100, recSleepTime = 100;
    int mapSleepCount = 1, reduceSleepCount = 1;
    String suspend = "false";
    int numIters = 1;
    
    for(int i=0; i < args.length; i++ ) {
      if(args[i].equals("-m")) {
        numMapper = Integer.parseInt(args[++i]);
      }
      else if(args[i].equals("-r")) {
        numReducer = Integer.parseInt(args[++i]);
      }
      else if(args[i].equals("-mt")) {
        mapSleepTime = Long.parseLong(args[++i]);
      }
      else if(args[i].equals("-rt")) {
        reduceSleepTime = Long.parseLong(args[++i]);
      }
      else if (args[i].equals("-recordt")) {
        recSleepTime = Long.parseLong(args[++i]);
      }
      else if (args[i].equals("-suspend")) {
        suspend = args[++i];
      }
      else if (args[i].equals("-iters")) {
        numIters = Integer.parseInt(args[++i]);
      }
    }
    
    // sleep for *SleepTime duration in Task by recSleepTime per record
    mapSleepCount = (int)Math.ceil(mapSleepTime / ((double)recSleepTime));
    reduceSleepCount = (int)Math.ceil(reduceSleepTime / ((double)recSleepTime));
    Job job = createJob(numMapper, numReducer, mapSleepTime,
                mapSleepCount, reduceSleepTime, reduceSleepCount);
    job.submit();
    
    if (!suspend.contains("false")) {
      JobID jobId = job.getJobID();
      TaskAttemptID taskAttemptId = new TaskAttemptID(jobId.getJtIdentifier(), jobId.getId(), TaskType.REDUCE, 0, 0);
      (new SuspendRunner(mapSleepTime, reduceSleepTime, suspend, numIters, taskAttemptId, job)).start();
    }
    
    return job.waitForCompletion(true) ? 0 : 1;
  }
  
  public static class SuspendRunner extends Thread {
    long mapSleepTime;
    long reduceSleepTime;
    String suspend;
    int numIters;
    TaskAttemptID taskAttemptId;
    Job job;
    
    public SuspendRunner(long mapSleepTime, long reduceSleepTime,
      String suspend, int numIters, TaskAttemptID taskAttemptId, Job job) {
      this.mapSleepTime = mapSleepTime;
      this.reduceSleepTime = reduceSleepTime;
      this.suspend = suspend;
      this.numIters = numIters;
      this.taskAttemptId = taskAttemptId;
      this.job = job;
    }
    
    public void run() {
      System.out.println("Sleeping mapSleepTime "+mapSleepTime);
      try {
        Thread.sleep(mapSleepTime);
      } catch (InterruptedException e1) {
        System.err.println("Sleep interrupted");
        e1.printStackTrace();
      }
      long interval = (reduceSleepTime+reduceSleepTime)/(numIters+1);
      System.out.println("(bcho2): calling "+suspend+" (with interval "+interval+", "+numIters+ "times)");
      for (int i = 0; i < numIters; i++) {
        System.out.println("Sleeping interval "+interval);
        try {
          Thread.sleep(interval);
        } catch (InterruptedException e) {
          System.err.println("Sleep interrupted");
          e.printStackTrace();
        }

        System.out.println("(bcho2) calling "+suspend);
        try {
          if (suspend.contains("suspend")) {
              job.suspendTask(taskAttemptId);
              Thread.sleep(5000);
              job.resumeTask(taskAttemptId.getTaskID());
          } else if (suspend.contains("kill")) {
            job.killTask(taskAttemptId);
          } else if (suspend.contains("fail")) {
            job.failTask(taskAttemptId);
          } else {
            System.out.println("Unknown suspend option "+suspend);
            break;
          }
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("Exception, break");
          break;
        } catch (InterruptedException e) {
          e.printStackTrace();
          System.err.println("Exception, break");
          break;
        }
        System.out.println("(bcho2) done calling "+suspend);
      }
    }
  }
}
