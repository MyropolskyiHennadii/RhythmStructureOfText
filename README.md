# RythmStructureOfText
Vocal And Rythm Structure of Text

Every text, not only poems, has its own vocale(probably rythmic) structure - regular or not regular stresses.
For instance we want to recognize poetry meter in poem (trochee, iambus and so on), but not only.
We schould represent the whole text as a sequence of symbols 1 and 0, where 1 is syllable under the stress and 0 - without one. 
Then we schould do with this sequence everything we want.

So the first task is to split text (or file content) into segments (paragraphs or/and lines or/and sentences and words).
Then we have to recognize stresses in segments and build sequence of symbols 1 and 0 for each segment.
And then - if we have a poem - we use to detect poetry meter and its characteristics. It may be not so simple challenge.

Those purposes for Russian language are treated here.

The problems with Russian texts rythmic structure are
1. there is no Russian online-vocabulary with simple stresses-schema like 00010101010...
2. the stress position in Russian poems often is variable.

Unfortunately I have Russian vocabulary of stresses only offline, but I hope soon have a hosting for this vocabulary (<4Gb) and web application.

But code is here.


