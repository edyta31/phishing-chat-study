import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

//import { AppComponent } from './app.component';
//import { ChatbotComponent } from '../../src/app/components/chatbot/chatbot.component';
//import { TaskComponent } from '../../src/app/components/task-display/task-display.component';
//import { QuestionnaireComponent } from '../../src/app/components/questionnaire/questionnaire.component';

@NgModule({
    declarations: [
        //AppComponent,
        //ChatbotComponent,
        //TaskComponent,
        //QuestionnaireComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule
    ],
    providers: [],
    bootstrap: [//AppComponent
     ]
})
export class AppModule { }
