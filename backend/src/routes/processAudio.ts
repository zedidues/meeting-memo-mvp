import { Router } from "express";
import multer from "multer";
import { summarizeMeeting } from "../services/llmService";
import { transcribeAudio } from "../services/speechToTextService";
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
  limits: {
    fileSize: maxAudioFileSizeBytes,
  },
});

export const processAudioRouter = Router();

processAudioRouter.post("/", upload.single("file"), async (request, response) => {
  try {
    const file = request.file;
    if (!file) {
      response.status(400).json({
        message: "Multipart request must include a file field.",
      });
      return;
    }

    validateAudioFile(file.originalname, file.mimetype, file.size);

    const transcript = await transcribeAudio({
      fileName: file.originalname,
      mimeType: file.mimetype,
      bytes: file.buffer,
    });

    const summary = await summarizeMeeting(transcript);
    const result: AudioProcessResult = {
      title: summary.title,
      transcript,
      summary: summary.summary,
      actionItems: summary.actionItems,
    };

    response.status(200).json(result);
  } catch (error) {
    if (error instanceof multer.MulterError && error.code === "LIMIT_FILE_SIZE") {
      response.status(413).json({
        message: "Audio file size exceeds the 25MB limit.",
      });
      return;
    }

    response.status(500).json({
      message: error instanceof Error ? error.message : "Unknown audio processing error",
    });
  }
});

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
