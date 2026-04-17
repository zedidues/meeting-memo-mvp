import { Router } from "express";
import multer from "multer";
import { summarizeMeeting } from "../services/llmService";
import { transcribeAudio } from "../services/speechToTextService";
import { createJob, updateJob } from "../services/jobStore";
import { AudioProcessResult } from "../types/api";

const maxAudioFileSizeBytes = 25 * 1024 * 1024;
const supportedExtensions = new Set(["m4a", "mp3", "wav", "aac", "ogg"]);
const supportedMimeTypes = new Set([
  "audio/m4a",
  "audio/mp4",
  "audio/mpeg",
  "audio/mp3",
  "audio/wav",
  "audio/x-wav",
  "audio/aac",
  "audio/ogg",
  "application/octet-stream",
]);

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: maxAudioFileSizeBytes },
});

export const processAudioRouter = Router();

processAudioRouter.post("/", upload.single("file"), async (request, response) => {
  try {
    const file = request.file;
    if (!file) {
      response.status(400).json({ message: "Multipart request must include a file field." });
      return;
    }

    validateAudioFile(file.originalname, file.mimetype, file.size);

    const job = createJob();
    response.status(202).json({ jobId: job.id });

    processInBackground(job.id, file.originalname, file.mimetype, file.buffer);
  } catch (error) {
    if (error instanceof multer.MulterError && error.code === "LIMIT_FILE_SIZE") {
      response.status(413).json({ message: "Audio file size exceeds the 25MB limit." });
      return;
    }
    response.status(500).json({
      message: error instanceof Error ? error.message : "Unknown audio processing error",
    });
  }
});

async function processInBackground(
  jobId: string,
  fileName: string,
  mimeType: string,
  buffer: Buffer,
): Promise<void> {
  try {
    updateJob(jobId, { status: "transcribing" });
    const transcript = await transcribeAudio({ fileName, mimeType, bytes: buffer });

    updateJob(jobId, { status: "summarizing" });
    const summary = await summarizeMeeting(transcript);

    const result: AudioProcessResult = {
      title: summary.title,
      transcript,
      summary: summary.summary,
      actionItems: summary.actionItems,
    };
    updateJob(jobId, { status: "completed", result });
  } catch (error) {
    updateJob(jobId, {
      status: "failed",
      error: error instanceof Error ? error.message : "Unknown processing error",
    });
  }
}

function validateAudioFile(fileName: string, mimeType: string, sizeBytes: number): void {
  const extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  if (!supportedExtensions.has(extension)) {
    throw new Error("Unsupported audio format. Use m4a, mp3, wav, aac, or ogg.");
  }
  if (!supportedMimeTypes.has(mimeType)) {
    throw new Error("Unsupported audio MIME type.");
  }
  if (sizeBytes > maxAudioFileSizeBytes) {
    throw new Error("Audio file size exceeds the 25MB limit.");
  }
}
