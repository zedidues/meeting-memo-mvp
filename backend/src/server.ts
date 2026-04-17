import "dotenv/config";
import cors from "cors";
import express from "express";
import multer from "multer";
import { env } from "./config/env";
import { processAudioRouter } from "./routes/processAudio";
import { sendEmailRouter } from "./routes/sendEmail";
import { summaryRouter } from "./routes/summary";

const app = express();

app.use(cors());
app.use(express.json({ limit: "1mb" }));
app.use(express.urlencoded({ extended: true }));

app.get("/health", (_request, response) => {
  response.status(200).json({ ok: true });
});

app.use("/summary", summaryRouter);
app.use("/sendEmail", sendEmailRouter);
app.use("/processAudio", processAudioRouter);

app.use((error: unknown, _request: express.Request, response: express.Response, _next: express.NextFunction) => {
  if (error instanceof multer.MulterError && error.code === "LIMIT_FILE_SIZE") {
    response.status(413).json({
      message: "Audio file size exceeds the 25MB limit.",
    });
    return;
  }

  response.status(500).json({
    message: error instanceof Error ? error.message : "Unknown server error",
  });
});

app.listen(env.port, () => {
  console.log(`Meeting Memo backend listening on port ${env.port}`);
});
