/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {EntityState, guid} from '@datorama/akita';

export type ModalSize = 'sm'|'md'|'lg'|'fl';

export interface Modal {
  id: string;
  uuid: string;
  size: ModalSize;
}

export interface ModalWithReturnValue<T> extends Modal{
  returnValue: T;
  hasReturnValue: true;
}

export function hasReturnValue<T>(value: Modal): value is ModalWithReturnValue<T> {
  return (value as any).hasReturnValue == true;
}

export interface ModalState extends EntityState<Modal, string> {}

/**
 * A factory function that creates Modal
 */
export function createModal<T>(params: Partial<Modal> & {id: string}): Modal {
  return {
    uuid: guid(),
    size: 'md',
    ...params
  };
}
