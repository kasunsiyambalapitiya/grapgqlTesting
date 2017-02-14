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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
    

    //=========== calling the relevant API and saving the output to a file===============================================

    public void  callingTheAPI(String URL, String file,boolean requireToken,boolean requireCommitHeader,boolean requireReviewHeader) throws IOException{

        BufferedReader bufferedReader= null;
        CloseableHttpClient httpclient= null;
        CloseableHttpResponse httpResponse= null;
        BufferedWriter bufferedWriter= null;


        //================ To do: 
        //                try(BufferedReader bufferedReader= new BufferedReader(new InputStreamReader (httpResponse.getEntity().getContent()))){
        //                    StringBuilder stringBuilder= new StringBuilder();
        //                    String line;
        //                    while((line=bufferedReader.readLine())!=null){
        //                        stringBuilder.append(line);
        //        
        //                    }
        //        
        //                    System.out.println(stringBuilder.toString()); 
        //        
        //        
        //                }


        try {
            httpclient = HttpClients.createDefault();
            HttpGet httpGet= new HttpGet(URL);

            if(requireToken==true){

                httpGet.addHeader("Authorization","Bearer "+getToken());        // passing the token for the API call
            }

            //as the accept header is needed for the review API since it is still in preview mode   
            if(requireReviewHeader==true){
                httpGet.addHeader("Accept","application/vnd.github.black-cat-preview+json");

            }

            //as the accept header is needed for accessing commit search API which is still in preview mode
            if(requireCommitHeader==true){
                httpGet.addHeader("Accept","application/vnd.github.cloak-preview");
            }

            httpResponse=httpclient.execute(httpGet);
            int responseCode= httpResponse.getStatusLine().getStatusCode();     // to get the response code

            //System.out.println("Response Code: "+responseCode);

            bufferedReader= new BufferedReader(new InputStreamReader (httpResponse.getEntity().getContent()));

            StringBuilder stringBuilder= new StringBuilder();
            String line;
            while((line=bufferedReader.readLine())!=null){
                stringBuilder.append(line);

            }

            //System.out.println("Recieved JSON "+stringBuilder.toString());

            //------- writing the content received from the response to the given file location------------

            File fileLocator= new File(location+file);
            fileLocator.getParentFile().mkdirs();
            bufferedWriter= new BufferedWriter(new FileWriter (fileLocator));
            bufferedWriter.write(stringBuilder.toString());
        } 

        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        finally{
            if(bufferedWriter != null){
                bufferedWriter.close();
            }

            if (bufferedReader != null){
                bufferedReader.close();
            }

            if(httpResponse != null){
                httpResponse.close();
            }
            if (httpclient != null){
                httpclient.close();
            }


        }
    }
}
