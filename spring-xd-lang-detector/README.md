Spring XD Language Detector Example
===================================

This is an example of a custom module that uses the [langdetect](https://code.google.com/p/language-detection/) API.

## Requirements

In order to install the module run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x [Instructions](https://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started)

## Code Tour

The heart of the sample is the processing module named LanguageDetectionProcessor.java.  This uses the langdetect API to detect the language of a text property in the given input.  The tuple data type is used as a generic container for keyed data.


## Building with Maven

	$ mvn clean package

## Building with Gradle

	$./gradlew clean test bootRepackage

## Using the Custom Module

The uber-jar will be in `[project build dir]/xd-langdetect-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


	_____                           __   _______
	/  ___|          (-)             \ \ / /  _  \
	\ `--. _ __  _ __ _ _ __   __ _   \ V /| | | |
 	`--. \ '_ \| '__| | '_ \ / _` |  / ^ \| | | |
	/\__/ / |_) | |  | | | | | (_| | / / \ \ |/ /
	\____/| .__/|_|  |_|_| |_|\__, | \/   \/___/
    	  | |                  __/ |
      	|_|                 |___/

	eXtreme Data
	1.1.0.BUILD-SNAPSHOT | Admin Server Target: http://localhost:9393
	Welcome to the Spring XD shell. For assistance hit TAB or type "help".

	xd:>module upload --file [path-to]/xd-langdetect-1.0.0.BUILD-SNAPSHOT.jar --name langdetect --type processor
	Successfully uploaded module 'processor:langdetect'

The module configuration:

     xd:>module info --name processor:langdetect
     Information about processor module 'langdetect':

       Option Name                              Description                                                                                        Default          Type
       ---------------------------------------  -------------------------------------------------------------------------------------------------  ---------------  ---------
       deterministicLanguageDetection           the same language and probability is returned for the same text if enabled                             false            boolean
       inputTextContentPropertyName             the name of the property that contains the input text                                              text             String
       languagePriorities                       allows to prioritize languages via pattern, e.g. en:0.1,de:0.1,fr:0.1                              <none>           String
       languageProbabilitiesOutputPropertyName  the name of the output property the detected language probabilities are written to                 pred_lang_probs  String
       languageProfileLocation                  the location of the language model. If empty we fall back to the profiles shipped with langdetect                   String
       mostLikelyLanguageOutputPropertyName     the name of the output property the detected language is written to                                pred_lang        String
       returnLanguageProbabilities              outputs the detected language probabilities as a list if enabled                                   false            boolean
       returnMostLikelyLanguage                 returns the most likely detected language if enabled                                               true             boolean
       textModel                                the name of the text model that should be used either SHORTTEXT or LONGTEXT                        SHORTTEXT        TextModel
       outputType                               how this module should emit messages it produces                                                   <none>           MimeType
       inputType                                how this module should interpret messages it consumes                                              <none>           MimeType                                         <none>           MimeType



Now create an deploy a stream:

```
xd:>stream create langdetect-demo --definition "http | langdetect --inputType=application/x-xd-tuple --returnLanguageProbabilities=true | log" --deploy
```

To post several messages, use the script file generateData.script located in this repository.

```
xd:>script --file [path-to]/generateData.script
```

This will post JSON data such as  `{"id":"1","text":"Hello World"}` to the stream.  The use of the inputType option (all modules have this option) instructs XD to convert the JSON string to an XD Tuple object before invoking the process method.

You can also post data yourself like:

    http post --data '{"id":"1","text":"Hello World"}' --contentType application/json
    http post --data '{"id":"2","text":"Hallo Welt"}' --contentType application/json
    http post --data '{"id":"3","text":"Santo maccheroni"}' --contentType application/json
    http post --data '{"id":"4","text":"Bonjour Howdy"}' --contentType application/json


You should see the stream output in the Spring XD log:

```
2015-03-10 17:19:23,663 1.2.0.SNAP  INFO pool-39-thread-4 sink.langdetect-demo - {"id":"72715141-20db-4732-0058-d9110312d981","timestamp":1426004363660,"text":"Hello World","pred_lang":"en","pred_lang_probs":[{"lang":"en","prob":0.9999941901768891}]}
2015-03-10 17:19:23,691 1.2.0.SNAP  INFO pool-39-thread-6 sink.langdetect-demo - {"id":"060571a6-5faf-0ebb-a76a-e46d305126ea","timestamp":1426004363690,"text":"Hallo Welt","pred_lang":"de","pred_lang_probs":[{"lang":"de","prob":0.9999954806050632}]}
2015-03-10 17:19:23,715 1.2.0.SNAP  INFO pool-39-thread-8 sink.langdetect-demo - {"id":"3aece5d3-0c1c-04ea-6feb-44e0ebbe60c2","timestamp":1426004363714,"text":"Santo maccheroni","pred_lang":"it","pred_lang_probs":[{"lang":"it","prob":0.9999972156431317}]}
2015-03-10 17:19:23,738 1.2.0.SNAP  INFO pool-39-thread-10 sink.langdetect-demo - {"id":"0d6b6bc8-be3e-63f0-71ba-cd7071651be6","timestamp":1426004363738,"text":"Bonjour Howdy","pred_lang":"en","pred_lang_probs":[{"lang":"en","prob":0.9999971857155178}]}
```

To destroy the stream

```
xd:> stream destroy --name langdetect-demo
```

To delete the module (Note that the stream must be undeployed first!)

```
module delete --name processor:langdetect
```