=> Spring Batch CSV to Database Import <=

This project demonstrates how to use Spring Batch to read customer data from a CSV file and import it into a relational database. 
It defines a simple batch job (importCustomers) consisting of a single step (csv-step) that performs the following:

Reader: Reads customer records from a CSV file using FlatFileItemReader.
Processor: Optionally transforms or validates each record.
Writer: Saves valid customer records into the database using a Spring Data JPA repository.

The batch job is configured to run asynchronously using a TaskExecutor, and it can be triggered via a REST controller.
The CSV file contains fields such as id, firstName, lastName, email, gender, contactNo, country, and dob.

The configuration is modular and follows best practices with support for static or dynamic file loading.
