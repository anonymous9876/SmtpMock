export interface EmailAddressGroup {
  label: string;
  values: string[];
}

export interface Email {
  id: string;
  from: string | null;
  to: string[];
  cc: string[];
  bcc: string[];
  subject: string | null;
  body: string | null;
  receivedAt: string;
  rawMessage: string;
}
