# RhythmStructureOfText
Vocal And Rythm Structure of Text

Every text, not only poems, has its own vocal (probably rhythmic) structure - regular or not regular stresses.
For instance we want to recognize poetry meter in a poem (trochee, iambus and so on), but not only.
We should represent the whole text as a sequence of symbols 1 and 0, where 1 is the syllable under the stress and 0 - without one. 
Then we should do with this sequence everything we want.

So the first task is to split text (or file content) into segments (paragraphs or/and lines or/and sentences and words).
Then we have to recognize stresses in segments and build sequence of symbols 1 and 0 for each segment.
And then - if we have a poem - we use to detect poetry meter and its characteristics. It may be not such a simple challenge.

Those purposes for the Russian language are treated here.

The problems with the Russian texts rhythmic structure are
1. there is no Russian online-vocabulary with simple stresses-schema like 00010101010...
2. the stress position in the Russian poems is often variable.

I don't know how long the application will be available here http://94.130.181.51:8090/rhythm, but you can try.


