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

export interface SendEmailRequestBody {
  toEmail: string;
  title: string;
  rawText: string;
  summaryText: string;
}
