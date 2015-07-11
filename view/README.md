View
====

View visualizations of extractions and NLP annotations. Search by keywords.

Run ./setup.sh to install.

Make sure you run `source env.sh` each time you run view.

You can use `./run.sh` (in rightmost iterm2 tab) to run the development setup.

To update view's index, adjust `view.conf` and run tools in `./util`.

Ideal input is [Pipe](../pipe)'s column format. However, you can also create input from DeepDive's `sentences` table using `./fetch-sentences-table.py'. Our setup has been tested with DeepDive's `deepdive_spouse_tsv` example.

Then fetch extractor output by running `./fetch-anntations.py`.

Then create the indexes:

```
./create_index.sh
./refresh-documents.py
./refresh-annotations.py
``` 

Visit `http://localhost:3000`.
