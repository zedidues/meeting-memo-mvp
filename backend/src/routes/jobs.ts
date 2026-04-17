import { Router } from "express";
import { getJob } from "../services/jobStore";

export const jobsRouter = Router();

jobsRouter.get("/:jobId", (request, response) => {
  const job = getJob(request.params.jobId);
  if (!job) {
    response.status(404).json({ message: "Job not found." });
    return;
  }
  response.status(200).json({
    jobId: job.id,
    status: job.status,
    result: job.result ?? null,
    error: job.error ?? null,
  });
});
