/*
*  Copyright (c) Feb 14, 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/



package graphqlTesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.parser.JSONParser;

public class Demo {
    private String token;
    private String patchId;
    private String urlForObtainingCommitHashes,urlForObtainingPRs;

    protected final String location=System.getProperty("user.dir")+"/";           // to save the json output of the API call
    private String jsonOutPutFileOfCommits= "jsonOutPutFileCommits.json";
    private String jsonOutPutFileOfPRs= "jsonOutPutFilePRs.json";

    private String prHtmlUrlDetails;
    protected JSONParser parser= new JSONParser();
    protected String patchInformation_svnRevisionpublic[];        // for saving the commit id of the patch

    protected ArrayList <String> productID= new ArrayList<String>(); 
    protected ArrayList <Long> prNumber = new ArrayList <Long> ();



    Scanner user_input= new Scanner(System.in);




    public String getToken() {
        return token;
    }

    public void setToken(String tokenFor) {
        System.out.println("Enter the token for "+ tokenFor);

        this.token= user_input.next();

    }

    public String getPatchId() {
        return patchId;
    }
    public void setPatchId(String patchId) {
        this.patchId = patchId;
    }


    public String getURL() {
        return urlForObtainingCommitHashes;
    }
    public void setURL(String uRL) {
        urlForObtainingCommitHashes = uRL;
    }



    // =============== for setting the internal PMT API URL ============================================================
    public void setData() throws IOException{

        System.out.println("Enter the patch id");

        setPatchId(user_input.next());

        setURL("http://umt.private.wso2.com:9765/codequalitymatricesapi/1.0.0//properties?path=/_system/governance/patchs/"+getPatchId());

//        callingTheAPI(urlForObtainingCommitHashes,jsonOutPutFileOfCommits,true,false,false);

    }
}
