package textspeech.thezaxis.speechtext;

import android.os.AsyncTask;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class RequestBuilder {
    Result result;
    public Result getResponse(final AIDataService aiDataService, final AIRequest aiRequest){
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    result = aiResponse.getResult();
                }
            }
        }.execute(aiRequest);
        return result;
    }
}
