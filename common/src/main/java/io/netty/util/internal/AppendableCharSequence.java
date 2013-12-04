/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;


import java.util.Arrays;

public final class AppendableCharSequence implements CharSequence, Appendable {
    private char[] chars;
    private int pos;

    public AppendableCharSequence(int length) {
        chars = new char[length];
    }

    private AppendableCharSequence(char[] chars) {
        this.chars = chars;
    }

    @Override
    public int length() {
        return pos;
    }

    @Override
    public char charAt(int index) {
        if (index > pos) {
            throw new IndexOutOfBoundsException();
        }
        return chars[index];
    }

    @Override
    public AppendableCharSequence subSequence(int start, int end) {
        return new AppendableCharSequence(Arrays.copyOfRange(chars, start, end));
    }

    @Override
    public AppendableCharSequence append(char c) {
        if (pos == chars.length) {
            char[] old = chars;
            // double it
            int len = old.length << 1;
            if (len < 0) {
                throw new IllegalStateException();
            }
            chars = new char[len];
            System.arraycopy(old, 0, chars, 0, old.length);
        }
        chars[pos++] = c;
        return this;
    }

    @Override
    public AppendableCharSequence append(CharSequence csq) {
        return append(csq, 0, csq.length());
    }

    @Override
    public AppendableCharSequence append(CharSequence csq, int start, int end) {
        if (csq.length() < end) {
            throw new IndexOutOfBoundsException();
        }
        int length = end - start;
        if (length > chars.length - pos) {
            chars = expand(chars, pos + length, pos);
        }
        if (csq instanceof AppendableCharSequence) {
            // Optimize append operations via array copy
            AppendableCharSequence seq = (AppendableCharSequence) csq;
            char[] src = seq.chars;
            System.arraycopy(src, start, chars, pos, length);
            pos += length;
            return this;
        }
        for (int i = start; i < end; i++) {
            chars[pos++] = csq.charAt(i);
        }

        return this;
    }

    /**
     * Reset the {@link AppendableCharSequence}. Be aware this will only reset the current internal position and not
     * shrink the internal char array.
     */
    public void reset() {
        pos = 0;
    }

    @Override
    public String toString() {
        return new String(chars, 0, pos);
    }

    /**
     * Create a new {@link String} from the given start to end.
     */
    public String substring(int start, int end) {
        int length = end - start;
        if (start > pos || length > pos) {
            throw new IndexOutOfBoundsException();
        }
        return new String(chars, start, length);
    }

    private static char[] expand(char[] array, int neededSpace, int size) {
        int newCapacity = array.length;
        do {
            // double capacity until it is big enough
            newCapacity <<= 1;

            if (newCapacity < 0) {
                throw new IllegalStateException();
            }

        } while (neededSpace > newCapacity);

        char[] newArray = new char[newCapacity];
        System.arraycopy(array, 0, newArray, 0, size);

        return newArray;
    }
}