/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.graalvm.visualvm.lib.jfluid.wireprotocol;

import org.graalvm.visualvm.lib.jfluid.global.CommonConstants;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This response is generated by the back end and contains the  VM properties such as various class paths.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class VMPropertiesResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String bootClassPath;
    private String javaClassPath;
    private String javaCommand;
    private String javaExtDirs;
    private String jdkVersionString;
    private String jvmArguments;
    private String targetMachineOSName;
    private String workingDir;
    private boolean canInstrumentConstructor;
    private int agentId;
    private int agentVersion;
    private long maxHeapSize;
    private long startupTimeInCounts;
    private long startupTimeMillis;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public VMPropertiesResponse(String jdkVerString, String javaClassPath, String javaExtDirs, String bootClassPath,
                                String workingDir, String jvmArguments, String javaCommand, String targetMachineOSName,
                                boolean canInstrumentConstructor, long maxHeapSize, long startupTimeMillis,
                                long startupTimeInCounts, int agentId) {
        super(true, VM_PROPERTIES);
        this.jdkVersionString = jdkVerString;
        this.javaClassPath = javaClassPath;
        this.javaExtDirs = javaExtDirs;
        this.bootClassPath = bootClassPath;
        this.workingDir = workingDir;
        this.jvmArguments = (jvmArguments != null) ? jvmArguments : ""; // NOI18N
        this.javaCommand = (javaCommand != null) ? javaCommand : ""; // NOI18N
        this.targetMachineOSName = targetMachineOSName;
        this.canInstrumentConstructor = canInstrumentConstructor;
        this.maxHeapSize = maxHeapSize;
        this.startupTimeMillis = startupTimeMillis;
        this.startupTimeInCounts = startupTimeInCounts & 0xFFFFFFFFFFFFFFL; // we use only 7 bytes for hi res timer
        this.agentId = agentId;
        this.agentVersion = CommonConstants.CURRENT_AGENT_VERSION;
    }

    // Custom serialization support
    VMPropertiesResponse() {
        super(true, VM_PROPERTIES);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getAgentId() {
        return agentId;
    }

    public int getAgentVersion() {
        return agentVersion;
    }

    public String getBootClassPath() {
        return bootClassPath;
    }

    public String getJDKVersionString() {
        return jdkVersionString;
    }

    public String getJVMArguments() {
        return jvmArguments;
    }

    public String getJavaClassPath() {
        return javaClassPath;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public String getJavaExtDirs() {
        return javaExtDirs;
    }

    public boolean canInstrumentConstructor() {
        return canInstrumentConstructor;
    }

    public long getMaxHeapSize() {
        return maxHeapSize;
    }

    public long getStartupTimeInCounts() {
        return startupTimeInCounts;
    }

    public long getStartupTimeMillis() {
        return startupTimeMillis;
    }

    public String getTargetMachineOSName() {
        return targetMachineOSName;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    // For debugging
    public String toString() {
        return "VMPropertiesResponse:" // NOI18N
               + "\n  jdkVersionString: " + jdkVersionString // NOI18N
               + "\n  javaClassPath: " + javaClassPath // NOI18N
               + "\n  javaExtDirs: " + javaExtDirs // NOI18N
               + "\n  bootClassPath: " + bootClassPath // NOI18N
               + "\n  workingDir: " + workingDir // NOI18N
               + "\n  jvmArguments: " + jvmArguments // NOI18N
               + "\n  javaCommand: " + javaCommand // NOI18N
               + "\n  targetMachineOSName: " + targetMachineOSName // NOI18N
               + "\n  canInstrumentConstructor: " + canInstrumentConstructor // NOI18N
               + "\n  maxHeapSize: " + maxHeapSize // NOI18N
               + "\n  startupTimeMillis: " + startupTimeMillis // NOI18N
               + "\n  startupTimeInCounts: " + startupTimeInCounts // NOI18N
               + "\n  agentId: " + agentId // NOI18N
               + "\n  agentVersion: " + agentVersion // NOI18N
               + "\n" + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        try {
            agentVersion = in.readInt();
            jdkVersionString = in.readUTF();
            javaClassPath = (String) in.readObject();
            javaExtDirs = in.readUTF();
            bootClassPath = in.readUTF();
            workingDir = in.readUTF();
            jvmArguments = in.readUTF();
            javaCommand = in.readUTF();
            targetMachineOSName = in.readUTF();
            canInstrumentConstructor = in.readBoolean();
            maxHeapSize = in.readLong();
            startupTimeMillis = in.readLong();
            startupTimeInCounts = in.readLong();
            agentId = in.readInt();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(agentVersion);
        out.writeUTF(jdkVersionString);
        out.writeObject(javaClassPath);
        out.writeUTF(javaExtDirs);
        out.writeUTF(bootClassPath);
        out.writeUTF(workingDir);
        out.writeUTF(jvmArguments);
        out.writeUTF(javaCommand);
        out.writeUTF(targetMachineOSName);
        out.writeBoolean(canInstrumentConstructor);
        out.writeLong(maxHeapSize);
        out.writeLong(startupTimeMillis);
        out.writeLong(startupTimeInCounts);
        out.writeInt(agentId);
    }
}
