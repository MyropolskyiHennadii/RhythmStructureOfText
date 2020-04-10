# RythmStructureOfText
Vocal And Rythm Structure of Text

Every text, not only poems, has its own rythmic structure - regular or not regular stresses.
For instance we want to recognize poetry meter in poem (trochee, iambus and so on), but not only.
We schould represent the whole text as a sequence of symbols 1 and 0, where 1 is syllable under the stress and 0 - without one. 
Then we schould do with this sequence everything we want.

So the first task is to split text (or file content) into segments (paragraphs or/and lines or/and sentences and words).
Then we have to recognize stresses in segments and build sequence of symbols 1 and 0 for each segment.
And then - in poem's case - we use to detect poetry meter and poems characteristics. It may be not so simple challenge.

These purposes for Russian language are treated here.

The problems with Russian text rythmic structure are
1. there is no Russian online-vocabulary with numerical stresses
2. the stress position in Rassian words often is variable.

Unfortunately I have Russian vocabulary of stresses only offline, but I hope soon have a hosting for this vocabulary (<4Gb).

But code is here. Only for poems. Only console. 
Prose is in process. Web-application too.




