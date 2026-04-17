import { randomUUID } from "crypto";
import { AudioProcessResult } from "../types/api";

export type JobStatus = "pending" | "transcribing" | "summarizing" | "completed" | "failed";

export interface Job {
  id: string;
  status: JobStatus;
  result?: AudioProcessResult;
  error?: string;
  createdAt: number;
}

const jobs = new Map<string, Job>();

setInterval(() => {
  const cutoff = Date.now() - 60 * 60 * 1000;
  for (const [id, job] of jobs.entries()) {
    if (job.createdAt < cutoff) jobs.delete(id);
  }
}, 10 * 60 * 1000).unref();

export function createJob(): Job {
  const job: Job = { id: randomUUID(), status: "pending", createdAt: Date.now() };
  jobs.set(job.id, job);
  return job;
}

export function updateJob(id: string, updates: Partial<Omit<Job, "id" | "createdAt">>): void {
  const job = jobs.get(id);
  if (job) Object.assign(job, updates);
}

export function getJob(id: string): Job | undefined {
  return jobs.get(id);
}
