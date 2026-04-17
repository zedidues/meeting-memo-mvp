import { onRequest } from "firebase-functions/v2/https";
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

export const processAudio = onRequest(async (request, response) => {
  setCors(response);

  if (request.method === "OPTIONS") {
    response.status(204).send("");
    return;
  }

  if (request.method !== "POST") {
    response.status(405).json({ message: "Method not allowed" });
    return;
  }

  try {
    const rawContentType = request.headers["content-type"];
    const contentType = Array.isArray(rawContentType)
      ? rawContentType[0] ?? ""
      : rawContentType ?? "";
    if (!contentType.includes("multipart/form-data")) {
      throw new AudioRequestError("Content-Type must be multipart/form-data", 400);
    }

    const file = parseMultipartFile(request.rawBody, contentType);
    validateAudioFile(file.fileName, file.mimeType, file.bytes.length);

    const transcript = await transcribeAudio({
      fileName: file.fileName,
      mimeType: file.mimeType,
      bytes: file.bytes,
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
    if (error instanceof AudioRequestError) {
      response.status(error.statusCode).json({ message: error.message });
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
    throw new AudioRequestError("Unsupported audio format. Use m4a, mp3, wav, aac, or ogg.", 400);
  }

  if (!supportedMimeTypes.has(mimeType)) {
    throw new AudioRequestError("Unsupported audio MIME type.", 400);
  }

  if (sizeBytes > maxAudioFileSizeBytes) {
    throw new AudioRequestError("Audio file size exceeds the 25MB limit.", 413);
  }
}

function parseMultipartFile(
  rawBody: Buffer,
  contentType: string,
): { fileName: string; mimeType: string; bytes: Buffer } {
  const boundaryMatch = contentType.match(/boundary=(?:"([^"]+)"|([^;]+))/i);
  const boundary = boundaryMatch?.[1] ?? boundaryMatch?.[2];
  if (!boundary) {
    throw new AudioRequestError("Missing multipart boundary.", 400);
  }

  const boundaryMarker = `--${boundary}`;
  const bodyText = rawBody.toString("latin1");
  const firstBoundaryIndex = bodyText.indexOf(boundaryMarker);
  if (firstBoundaryIndex < 0) {
    throw new AudioRequestError("Multipart boundary not found.", 400);
  }

  const headersStart = bodyText.indexOf("\r\n", firstBoundaryIndex) + 2;
  const headersEnd = bodyText.indexOf("\r\n\r\n", headersStart);
  if (headersEnd < 0) {
    throw new AudioRequestError("Multipart headers not found.", 400);
  }

  const headerSection = bodyText.slice(headersStart, headersEnd);
  const contentStart = headersEnd + 4;
  const nextBoundaryIndex = bodyText.indexOf(`\r\n${boundaryMarker}`, contentStart);
  if (nextBoundaryIndex < 0) {
    throw new AudioRequestError("Multipart content end not found.", 400);
  }

  const dispositionMatch = headerSection.match(
    /Content-Disposition:\s*form-data;\s*name="([^"]+)";\s*filename="([^"]+)"/i,
  );
  const partName = dispositionMatch?.[1];
  const fileName = dispositionMatch?.[2];
  if (partName !== "file" || !fileName) {
    throw new AudioRequestError("Multipart request must include a file field.", 400);
  }

  const mimeMatch = headerSection.match(/Content-Type:\s*([^\r\n]+)/i);
  const mimeType = mimeMatch?.[1]?.trim() || inferMimeTypeFromFileName(fileName);
  const bytes = rawBody.subarray(contentStart, nextBoundaryIndex);

  return {
    fileName,
    mimeType,
    bytes,
  };
}

function inferMimeTypeFromFileName(fileName: string): string {
  const extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  switch (extension) {
    case "m4a":
      return "audio/m4a";
    case "mp3":
      return "audio/mpeg";
    case "wav":
      return "audio/wav";
    case "aac":
      return "audio/aac";
    case "ogg":
      return "audio/ogg";
    default:
      return "application/octet-stream";
  }
}

function setCors(response: { set: (name: string, value: string) => void }): void {
  response.set("Access-Control-Allow-Origin", "*");
  response.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
  response.set("Access-Control-Allow-Methods", "POST, OPTIONS");
}

class AudioRequestError extends Error {
  constructor(
    message: string,
    readonly statusCode: number,
  ) {
    super(message);
  }
}
