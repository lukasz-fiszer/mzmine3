/*
 * Copyright 2006 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.taskcontrol.impl;

import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskController;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.Task.TaskPriority;
import net.sf.mzmine.taskcontrol.Task.TaskStatus;
import net.sf.mzmine.userinterface.Desktop;
import net.sf.mzmine.userinterface.components.TaskProgressWindow;
import net.sf.mzmine.userinterface.mainwindow.MainWindow;
import net.sf.mzmine.userinterface.mainwindow.Statusbar;

/**
 * Task controller implementation
 */
public class TaskControllerImpl implements TaskController, Runnable {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Desktop desktop;

    // TODO: always create a worker thread for high priority tasks

    private static TaskControllerImpl myInstance;

    private final int TASKCONTROLLER_THREAD_SLEEP = 100;

    private Thread taskControllerThread;

    private WorkerThread[] workerThreads;

    private TaskQueue taskQueue;

    /**
     * 
     */
    public TaskControllerImpl(int numberOfThreads) {

        assert myInstance == null;
        myInstance = this;

        taskQueue = new TaskQueue();

        taskControllerThread = new Thread(this, "Task controller thread");
        taskControllerThread.setPriority(Thread.MIN_PRIORITY);
        taskControllerThread.start();

        workerThreads = new WorkerThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            workerThreads[i] = new WorkerThread(i + 1, desktop);
            workerThreads[i].start();
        }

    }

    public void addTask(Task task) {
        addTask(task, TaskPriority.NORMAL, null);
    }

    public void addTask(Task task, TaskPriority priority) {
        addTask(task, priority, null);
    }

    public void addTask(Task task, TaskListener listener) {
        addTask(task, TaskPriority.NORMAL, listener);
    }

    public void addTask(Task task, TaskPriority priority, TaskListener listener) {

        assert task != null;

        WrappedTask newQueueEntry = new WrappedTask(task, priority, listener);

        logger.finest("Adding task \"" + task.getTaskDescription()
                + "\" to the task controller queue");

        taskQueue.addWrappedTask(newQueueEntry);

        synchronized (this) {
            this.notifyAll();
        }

        /*
         * show the task list component
         */
        MainWindow mainWindow = (MainWindow) desktop;
        if (mainWindow != null) {
            //JInternalFrame selectedFrame = desktop.getSelectedFrame();

            TaskProgressWindow tlc = mainWindow.getTaskList();
            tlc.setVisible(true);

            Statusbar sb = mainWindow.getStatusBar();
            sb.setProgressBarVisible(true);
            
            /*
            if ((selectedFrame != null) && (desktop.getSelectedFrame() == tlc)) {
                try {
                    selectedFrame.setSelected(true);
                } catch (PropertyVetoException e) {
                    // do nothing
                }
            }
            */
        }

    }

    /**
     * Task controller thread main method.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        while (true) {

            /* if the queue is empty, we can sleep */
            while (taskQueue.isEmpty()) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            WrappedTask[] queueSnapshot = taskQueue.getQueueSnapshot();

            // for each task, check if it's assigned and not canceled
            for (WrappedTask task : queueSnapshot) {

                if (!task.isAssigned() && (task.getTask().getStatus() != TaskStatus.CANCELED)) {
                    // poll local threads

                    for (WorkerThread worker : workerThreads) {

                        if (worker.getCurrentTask() == null) {
                            logger.finest("Assigning task \""
                                    + task.getTask().getTaskDescription()
                                    + "\" to " + worker.toString());
                            worker.setCurrentTask(task);
                            break;
                        }

                    }

                    // TODO: poll remote nodes

                }

            }

            // check if all tasks are finished
            if (taskQueue.allTasksFinished()) {

                MainWindow mainWindow = (MainWindow) desktop;

                if (mainWindow != null) {
                    TaskProgressWindow tlc = mainWindow.getTaskList();
                    tlc.setVisible(false);
                    Statusbar sb = mainWindow.getStatusBar();
                    sb.setProgressBarVisible(false);
                    taskQueue.clear();
                }

            } else {
                taskQueue.refresh();
            }

            try {
                Thread.sleep(TASKCONTROLLER_THREAD_SLEEP);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }

    public void setTaskPriority(Task task, TaskPriority priority) {
        WrappedTask wt = taskQueue.getWrappedTask(task);
        if (wt != null) {
            logger.finest("Setting priority of task \""
                    + task.getTaskDescription() + "\" to " + priority);
            wt.setPriority(priority);
            taskQueue.refresh();
        }
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public Task getTask(int index) {
        WrappedTask wt = taskQueue.getWrappedTask(index);
        if (wt != null)
            return wt.getTask();
        else
            return null;
    }

    /**
     */
    public void initModule(MZmineCore core) {
        this.desktop = core.getDesktop();

    }
    
    public static TaskController getInstance() {
        return myInstance;
    }

}
