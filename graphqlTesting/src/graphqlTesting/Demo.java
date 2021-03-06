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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



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

    //  ===================== getting the commit IDs from the above saved file ============================================

    public void getThePublicGitCommitId(){
        try{
            JSONArray jsonArray= (JSONArray)parser.parse(new FileReader(location+jsonOutPutFileOfCommits));

            for(int i=0; i<jsonArray.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                String tempName= (String)jsonObject.get("name");

                if(tempName.equals("patchInformation_svnRevisionpublic")){
                    JSONArray tempCommitsJSONArray= (JSONArray)jsonObject.get("value");

                    //initializing the patchInformation_svnRevisionpublic array

                    patchInformation_svnRevisionpublic= new String [tempCommitsJSONArray.size()];  

                    for(int j =0; j< tempCommitsJSONArray.size();j++){


                        patchInformation_svnRevisionpublic[j]=(String)tempCommitsJSONArray.get(j);


                    }

                    break;
                }

            }

            System.out.println("The commit Ids are");


            //            for printing all the commits ID associated with a patch
            for (String tmp: patchInformation_svnRevisionpublic){
                System.out.println(tmp);
            }
            System.out.println();

        }
        catch(FileNotFoundException e){
            System.out.println("JSON file is not found");
            e. printStackTrace();


        }
        catch (ParseException e){
            System.out.println("Parse Execption occured");
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}

class GettingBlameCommit extends Demo {

    private String jsonOutPutFileOfSearchCommitAPI="jsonOutPutFileOfSearchCommitAPI.json";
    private String urlForObtainingCommits,urlForGetingFilesChanged;

    protected ArrayList<String> fileNames = new ArrayList<String>();
    protected List<ArrayList<String>> lineRangesChanged= new ArrayList<ArrayList<String>>();      // for saving the line no that are changed
    JSONObject graphqlApiJsonObject= new JSONObject();





    private String repoLocation[];


    public String getUrlForSearchingCommits() {
        return urlForObtainingCommits;
    }

    public void setUrlForSearchingCommits(String commitHash) {
        this.urlForObtainingCommits = "https://api.github.com/search/commits?q=hash%3A"+commitHash;
    }

    public String getUrlForGetingFilesChanged() {
        return urlForGetingFilesChanged;
    }
    public void setUrlForGetingFilesChanged(String repoName,String commitHash) {
        this.urlForGetingFilesChanged ="http://api.github.com/repos/"+repoName+"/commits/"+commitHash;
    }

    //================ obtaining PR for each commit and saving them in a file ===================================
    public void obtainingRepoNamesForCommitHashes() throws IOException{


        for(String commitHash: patchInformation_svnRevisionpublic){

            setUrlForSearchingCommits(commitHash);


            //calling the API calling method
            callingTheAPI(getUrlForSearchingCommits(),jsonOutPutFileOfSearchCommitAPI,true,true,false);
            saveRepoNamesInAnArray(commitHash);

        }





    }
    //================================= saving the  Repo Names in the array and calling to Get files content========================================

    public void saveRepoNamesInAnArray(String commitHash){
        try{


            JSONObject rootJsonObject= (JSONObject)parser.parse(new FileReader(location+jsonOutPutFileOfSearchCommitAPI));
            JSONArray jsonArrayOfItems= (JSONArray)rootJsonObject.get("items");

            // setting the size of the repoLocationArray
            repoLocation= new String [jsonArrayOfItems.size()];

            for(int i=0; i<jsonArrayOfItems.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArrayOfItems.get(i);

                JSONObject repositoryJsonObject= (JSONObject)jsonObject.get("repository");

                //adding the repo name to the array
                repoLocation[i]= (String)repositoryJsonObject.get("full_name");

            }
        }

        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(ParseException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //        for running through the repoName Array 
        for(int i =0; i< repoLocation.length;i++){

            if(StringUtils.contains(repoLocation[i],"wso2/")){

                //clearing all the data in the current fileNames and lineRangesChanged arraylists for each repository
                fileNames.clear();
                lineRangesChanged.clear();


                callingToGetFilesChanged(repoLocation[i],commitHash); 


                //            calling the graphql API for getting blame information


                //====================================================================================================================================================
                //                try {

                //                    callingGraphqlApi(repoLocation[i],commitHash,false);
                //
                //                    // reading the blame thus received from graphql API
                //                    readingBlameOfFile(repoLocation[i],commitHash,false);





                //                } catch (IOException e) {
                //                   // TODO Auto-generated catch block
                //                    e.printStackTrace();
                //                }

                //====================================================================================================================================================


                iteratingOver(repoLocation[i],commitHash);




                //                =========================== testing new one ================================
            }
        }


    }

    public void callingToGetFilesChanged(String repoLocation, String commitHash){

        //        setting the URL for calling github single commit API

        setUrlForGetingFilesChanged(repoLocation,commitHash);

        //file name for saving the output
        String savingLocation= repoLocation+"/"+commitHash+".json";

        //saving the commit details for the commit hash on the relevant repository
        try {
            callingTheAPI(getUrlForGetingFilesChanged(), savingLocation, true,false,false);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 


        //----calling savingRelaventFileNamesAndEditLineNumbers method to read the above saved json output----
        savingRelaventFileNamesAndEditLineNumbers(savingLocation);





    }
    //    ======saving relevant file names and their edit line numbers =================================

    public void savingRelaventFileNamesAndEditLineNumbers(String savingLocation){

        //read the json output
        try {

            JSONObject rootJsonObject=(JSONObject) parser.parse(new FileReader(location+savingLocation));
            Object checkObject= rootJsonObject.get("files");
            //checking if the json stream received for element "files" is a jsonObject or a jsonArray

            if(checkObject instanceof JSONObject ){
                // if it is a JSONObject then only one file has been changed from the commit

                // then casting the checkObject to a JSONObject
                JSONObject filesJsonObject=(JSONObject)checkObject;
                String fileName=(String)filesJsonObject.get("filename");
                fileNames.add(fileName);

                //filtering only the line no that are modified

                String patch= (String)filesJsonObject.get("patch");
                String lineChanges[]=StringUtils.substringsBetween(patch,"@@", "@@");


            }

            // in genaral the code comes here--------------------------------------------
            else if (checkObject instanceof JSONArray){
                //                 more than one file has been changed by the relevant commit
                JSONArray fileJsonArray= (JSONArray)checkObject;

                // to save one file at a time
                for(int i =0; i< fileJsonArray.size();i++){
                    JSONObject tempJsonObject= (JSONObject) fileJsonArray.get(i);
                    String fileName= (String)tempJsonObject.get("filename");
                    //saving the file name in the filename arraylist
                    fileNames.add(fileName);

                    //filtering only the line ranges that are modified and saving to a string array
                    String patch= (String)tempJsonObject.get("patch");
                    String lineChanges[]= StringUtils.substringsBetween(patch,"@@","@@");

                    //filtering only the lines that existed in the previous file and saving them in to the same array
                    for (int j=0; j<lineChanges.length;j++){

                        String tempString= lineChanges[i];
                        String tempStringWithLinesBeingModified = StringUtils.substringBetween(tempString,"-"," +");

                        int intialLineNo= Integer.parseInt(StringUtils.substringBefore(tempStringWithLinesBeingModified, ","));
                        int tempEndLineNo= Integer.parseInt(StringUtils.substringAfter(tempStringWithLinesBeingModified,","));
                        int endLineNo= intialLineNo+ (tempEndLineNo-1);

                        // storing the line ranges that are being modified in the same array by replacing values
                        lineChanges[j]=intialLineNo+","+endLineNo;

                    }

                    ArrayList<String> tempArrayList= new ArrayList<String>(Arrays.asList(lineChanges));

                    //adding to the array list which keep track of the line ranges which are being changed to the main arrayList
                    lineRangesChanged.add(tempArrayList);







                }

                System.out.println("done saving file names and their relevant modification line ranges");
                System.out.println(fileNames);
                System.out.println(lineRangesChanged);


            }






        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }





    }

    //============================ iterating and calling graphql==================================================
    public void iteratingOver(String repoLocation, String commitHash){

        // filtering the owner and the repository name from the repoLocation
        String owner= StringUtils.substringBefore(repoLocation,"/");
        String repositoryName= StringUtils.substringAfter(repoLocation,"/");

        String locationOfTheSavedFile= null;


        //        iterating over the fileNames arraylist for the given commit
        Iterator iteratorForFileNames= fileNames.iterator();

        while(iteratorForFileNames.hasNext()){
            String fileName= (String)iteratorForFileNames.next();


            graphqlApiJsonObject.put("query","{repository(owner:\""+owner+"\",name:\""+repositoryName+"\"){object(expression:\""+commitHash+"\"){ ... on Commit{blame(path:\""+fileName+"\"){ranges{startingLine endingLine age commit{history(first: 2) { edges { node {  message url } } } author { name email } } } } } } } }");

            try{
                //            calling the graphql API for getting blame information for the current file and saving it in a location.
                locationOfTheSavedFile= callingGraphQl(graphqlApiJsonObject,fileName,commitHash,repoLocation);

            }
            catch(IOException e){
                e.printStackTrace();
            }
            //            reading the above saved output for the current file name



            //            readingTheBlameReceived(locationOfTheSavedFile,fileName,owner,repositoryName,repoLocation);






        }


    }
    public String callingGraphQl(JSONObject queryObject,String fileName,String commitHash,String repoLocation) throws IOException{

        CloseableHttpClient client= null;
        CloseableHttpResponse response= null;


        client= HttpClients.createDefault();
        HttpPost httpPost= new HttpPost("https://api.github.com/graphql");

        httpPost.addHeader("Authorization","Bearer "+getToken());
        httpPost.addHeader("Accept","application/json");

        try {

            //                StringEntity entity= new StringEntity("{\"query\":\"query "+graphqlQuery+"\"}");
            StringEntity entity= new StringEntity(queryObject.toString());


            httpPost.setEntity(entity);
            response= client.execute(httpPost);

        }

        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch(ClientProtocolException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }


        BufferedWriter bufferedWritter=null;
        BufferedReader bufferedReader=null;
        String saveLocation=null;
        try{
            bufferedReader= new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line= null;
            StringBuilder stringBuilder= new StringBuilder();
            while((line=bufferedReader.readLine())!= null){

                stringBuilder.append(line);

            }

            //renaming the file .java to _java as it may conflict with the .json extension (if not will result in 2 extensions for the same file)
            String fileNameWithoutExtension= StringUtils.substringBefore(fileName, ".");
            String fileNamesExtension= StringUtils.substringAfter(fileName,".");

            String modifiedFileName= fileNameWithoutExtension+"_"+fileNamesExtension;


            // saving the output received to a file located in the same repo directory under the same owner of the repo
            saveLocation= repoLocation+"/FileChanged/"+commitHash+"/"+modifiedFileName+"_blame.json";


            File fileLocation= new File(location+saveLocation);
            fileLocation.getParentFile().mkdirs();      //creating directories according to the file name given
            bufferedWritter= new BufferedWriter(new FileWriter(fileLocation));

            bufferedWritter.write(stringBuilder.toString());

            System.out.println(stringBuilder.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        finally{
            if(bufferedWritter !=null){
                bufferedWritter.close();

            }
            if(bufferedReader != null){
                bufferedReader.close();}
        }

        return saveLocation;





    }
}


