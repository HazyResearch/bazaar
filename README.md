Bazaar
======

Systems and apps supporting data services and APIs.

Run `setup.sh` to install dependencies and build the parser.

We assume that your input has the following format. There's one line per document and each document is a JSON object with a key and content field.
```json
{ "item_id":"doc1", "content":"Here is the content of my document.\nAnd here's another line." }
{ "item_id":"doc2", "content":"Here's another document." }
```

You can then run the NLP pipeline on 16 cores as follows:
```bash
./run_parallel.sh -in="input.json" --parallelism=16 -i json -k "item_id" -v "content"
```
The output will be files in tsv-format that you can directly load into the database.
