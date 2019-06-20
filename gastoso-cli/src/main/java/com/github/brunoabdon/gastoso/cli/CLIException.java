/*
 * Copyright (C) 2016 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.brunoabdon.gastoso.cli;

/**
 *
 * @author Bruno Abdon
 */
public class CLIException extends Exception{

    private static final long serialVersionUID = 3949285143338173748L;

    public CLIException() {
        super();
    }

    public CLIException(final String message) {
        super(message);
    }

    public CLIException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CLIException(final Throwable cause) {
        super(cause);
    }
}