export enum MessageType {
  Info = 'info',
  Error = 'error',
  Success = 'success',
  Warning = 'warning'
}

export interface IMessage {
  type: MessageType;
  content: string;
}

export interface ISortedMessages {
  type: MessageType;
  messages: IMessage[];
}
