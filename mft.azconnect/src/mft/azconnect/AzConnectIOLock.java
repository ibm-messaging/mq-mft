/**
 * Copyright (c) IBM Corporation 2018
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *  Contributors:
 *    Shashikanth Rao T - Initial Contribution
 *
 ***************************************************************************
 */
package mft.azconnect;

import java.io.IOException;

import com.ibm.wmqfte.exitroutine.api.IOExitLock;

public class AzConnectIOLock implements IOExitLock {

	boolean shared = false;
	@Override
	public boolean isShared() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void release() throws IOException {
		// TODO Auto-generated method stub

	}

}
