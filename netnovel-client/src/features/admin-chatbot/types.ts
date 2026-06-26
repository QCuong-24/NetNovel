export type LocalizedExamples = Record<string, string[]>;
export type LocalizedText = Record<string, string>;

export type ChatbotFaq = {
  id: string;
  type?: string | null;
  enabled?: boolean | null;
  priority?: number | null;
  examples: LocalizedExamples;
  answers: LocalizedText;
  actionUrls: string[];
  tags: string[];
};

export type ChatbotIntentAction = {
  labels: LocalizedText;
  type: string;
  value: string;
  requiredRoles?: string[] | null;
};

export type ChatbotIntent = {
  id: string;
  type?: string | null;
  enabled?: boolean | null;
  priority?: number | null;
  examples: LocalizedExamples;
  replies: LocalizedText;
  filters: Record<string, string>;
  tags: string[];
  actions: ChatbotIntentAction[];
};

export type ChatbotAdminSummary = {
  faqCount: number;
  intentCount: number;
  enabledFaqCount: number;
  enabledIntentCount: number;
};

export type ChatbotEmbeddingReindexResponse = {
  enabled: boolean;
  model: string;
  dimension: number;
  documents: number;
  batches: number;
  message: string;
};

export type ChatbotEmbeddingStatus = {
  enabled: boolean;
  model: string;
  dimension: number;
  totalDocuments: number;
  activeDocuments: number;
  faqDocuments: number;
  intentDocuments: number;
  lastIndexedAt?: string | null;
};
