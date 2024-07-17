/*-
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.ui.swing.task;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.task.Task;
import org.scijava.task.TaskService;
import org.scijava.ui.UIService;
import org.scijava.ui.swing.SwingUI;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Demo the monitoring of several parallel tasks.
 *
 * @author Nicolas Chiaruttini, 2022
 */

public class DemoTask {

    static int timeDownscalingFactor = 1; // Scales the time -> to run the demo faster
    static int nTasks = 5; // Number of tasks ran in the demo

    static Consumer<String> log; // = (str) -> System.out.println(str);

    public static void main(String[] args) {
        final Context context = new Context();
        context.service(UIService.class).showUI(SwingUI.NAME);
        final TaskService taskService = context.service(TaskService.class);
        final LogService logService = context.service(LogService.class);
        log = (message) -> logService.info(message);

        // A synchronous case
        for (int i=0;i<nTasks;i++) {
            int index = i;
            new Thread(() -> createTask(taskService, "Task_"+index, 4000, 100, 60000)).start();
        }

        // Synchronous case.
        AtomicBoolean syncTaskCancelled = new AtomicBoolean(false);
        Task syncTask = taskService.createTask("Synchronous task");
        syncTask.setCancelCallBack(() -> {
            log.accept("Task "+syncTask.getName()+" canceled: "+syncTask.getCancelReason());
            syncTaskCancelled.set(true);
        });
        syncTask.setProgressMaximum(100);
        syncTask.start();
        for (int i = 0; i<100; i++) {
            try {
                Thread.sleep(250/timeDownscalingFactor);
                syncTask.setProgressValue(i);
                //log.accept("Task "+syncTask.getName()+":"+syncTask.getProgressValue());
            } catch (InterruptedException e) {
                log.accept("Task "+syncTask.getName()+" interrupted!");
                return;
            }
            if (syncTaskCancelled.get()) {
                break;
            }
        }
        syncTask.finish();
        if (!syncTaskCancelled.get()) {
            log.accept("Task " + syncTask.getName() + " done!");
        }
    }

    public static Task createTask(
              TaskService taskService,
              String taskName,
              int msBeforeStart,
              int msUpdate,
              int msTaskDuration) {
        int msTD = msTaskDuration/timeDownscalingFactor;
        int msBS = msBeforeStart/timeDownscalingFactor;
        Task task = taskService.createTask(taskName);

        // Override the cancel callback by adding a message before calling the previous callback
        Runnable iniRunnable = task.getCancelCallBack(); // takes the initial callback
        task.setCancelCallBack(() -> {
            log.accept("Task "+task.getName()+" canceled: "+task.getCancelReason());
            iniRunnable.run(); // runs the callback
        });

        task.setProgressMaximum(100);

        try {
            Thread.sleep(msBS); // Waits before starting the task
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        task.run(() -> {
            int totalMs = 0;
            while (totalMs<msTD) {
                try {
                    Thread.sleep((int)(Math.random()*msUpdate));
                    totalMs += msUpdate;
                    task.setProgressValue((int) (((double) totalMs / msTD) * 100.0));
                    //log.accept("Task "+task.getName()+":"+task.getProgressValue());
                } catch (InterruptedException e) {
                    log.accept("Task "+task.getName()+" interrupted!");
                    return;
                }
            }
            log.accept("Task "+task.getName()+" done!");
        });

        return task;
    }

}
