export interface SummaryRequestBody {
  rawText: string;
}

export interface SummaryResult {
  title: string;
  summary: string;
  actionItems: string[];
}

export interface AudioProcessResult extends SummaryResult {
  transcript: string;
}

export interface JobCreatedResponse {
  jobId: string;
}

export interface JobStatusResponse {
  jobId: string;
  status: "pending" | "transcribing" | "summarizing" | "completed" | "failed";
  result: AudioProcessResult | null;
  error: string | null;
}

export interface SendEmailRequestBody {
  toEmail: string;
  title: string;
  rawText: string;
  summaryText: string;
}
