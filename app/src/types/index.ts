export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  profilePic?: string;
  role: "USER" | "ADMIN";
  address1?: string;
  city?: string;
  state?: string;
  pinCode?: string;
  dateOfBirth?: string;
  aadhaar?: string;
  isEmailVerified: boolean;
  kycStatus: "PENDING" | "APPROVED" | "REJECTED";
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface Account {
  id: string;
  name: string;
  mask: string;
  currentBalance: number;
  availableBalance: number;
  type: "DEPOSITORY" | "CREDIT" | "LOAN" | "INVESTMENT";
  subtype: "CHECKING" | "SAVINGS" | "CREDIT_CARD" | "MONEY_MARKET";
  loanDetails?: {
    principal: number;
    interestRate: number;
    termMonths: number;
    monthlyPayment: number;
  };
  creditDetails?: {
    creditLimit: number;
    statementBalance: number;
    apr: number;
  };
}

export interface Card {
  id: string;
  cardNumber: string; // Masked PAN
  cvv: string;
  expiryDate: string;
  status: "ACTIVE" | "FROZEN" | "CANCELLED";
  type: "PHYSICAL" | "VIRTUAL";
  subtype: "DEBIT" | "CREDIT";
}

export interface Transaction {
  id: string;
  accountId: string;
  amount: number;
  type: "TRANSFER" | "DEPOSIT" | "WITHDRAWAL";
  status: "COMPLETED" | "PENDING" | "FAILED";
  category: string;
  counterpartyName: string;
  timestamp: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
