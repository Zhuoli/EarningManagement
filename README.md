# Earning manager

The backend of Stock-shares assistant running at : http://zhuoliliang.com/em/helloWorld/current, it performs the realtime data updating and Sync so that the front end could always read the latest data from AWS Relational Database

### Functions
 1. Parse HTML content of Nasdaq website to get the realtime Stock price from : http://www.nasdaq.com/symbol/
 2. Periodically read Yahoo email box to check if has new order confimations from Robinhood, then extract the data from this new email and update to local HashMap and AWS database
 3. Keep track of the latest changes of Stock info and sync up between local HsahMap with AWS using TimeStamp
 4. Use JOOQ as Object-relational mapping(ORP)
 5. Maven based, integrated with AWS continuous integration tool - CodePipeline

## Design
Design can be found [here](https://www.draw.io/?lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=StockMonitorWorkflow#R7V1rc5s6Gv41ntn9kAySuH5M3PicnWnPdpqz0%2B1%2BOYNBttlg5ArcJPvrV1yEQRKYGLAdO51Oa2Rxey%2FPe5U8QdP1y2%2FU3ay%2BEB%2BHE6j5LxP0aQIhNE2D%2FZeOvBYj0HbykSUN%2FHxM2w08Bv%2FD%2BSDgo9vAx3Exlg8lhIRJsKkPeiSKsJfUruhSSp7r0xYk9GsDG3eJpYFHzw356K2xG%2F8e%2BMkqHzdsfTf%2BOw6Wq%2BLeNjTzL%2Bau97SkZBsVN5xANMv%2B5F%2BvXX6p4mnjleuT58oQepigKSUkyT%2BtX6Y4TKkr0C1OXvnDTtD9KlmH7ACwj9nXs4aTQZeT2ctRHNWI2nQ9ZDuG6%2Fl4Pgc2dh39xoL5JX654ZbfQbwl9hmti0NCkxVZksgNH3aj996W%2FsJ%2B8UwZMcujysPGiUuTu5TbbCAiEeZjsyBM52jsGEc%2Bn%2BGFbhwH3p%2BrIMq%2FKKall%2FovTpLXQgzdbULY0O7JPhOyKW9JyROekpDQ7F2Qlv0pKZe%2BmiBXO1Jy4pMt9YpZqJBtly5x0kTSnVgwhcNkjRP6yk6iOHST4Ff9fm4h%2BMty3o577EPBwAZmSqz7hl0%2F17JFsNxSdjsSTaDprhk97qN5vMle3AzZo9%2F7wS%2F2cZnkQ%2FmU%2FF8%2Bg92%2FPkk875HRAGqfyXKJaeeT%2BNCciiPiqYIYMs3bpB%2FXL8sUxW4XIXn2Vkx8bn03cdkZCyYfFVY7znTK1FgpBPrUccpvOF4gNsIu7QdMAiqzp9MHI7sO%2F%2B5TQBmGpaRNxZim594vSO0cTQMOQsU41x1BHRoF7hemCX6pDMmyVHx7YyIrP6cA7hsAjEIsnncoCOxi1qqCgOYQEijKPtB0iW%2BNeMDem77%2BO1X8Ww2UAz%2ByAYMffsU0YM%2BFaQEQZwUidflJsRm%2FBEn%2BSg4ExXH%2BRrplFMfiKx0GRboMRU3S0hl5ilO%2FkiBKdlIG7bqMmUi4RP4MxVmC9JSPcaBAmXarPSok4H3YHFlcBmO9gnDWkcyQLpmhf0RBErhhSiqofd3OwyBepRbiA9D3AzpwuCtbqpvpyIhuODKi21p%2FZkpyBJF5gEPYBfNNB9Yx30H2O0X9w9S4jJTa9Bgi%2FUh6XErrTpEruqv9uaKZd9lZiTcU%2B3gRRNj%2Fa0OJh%2BP4OlUaASQotKXLCu1oskI7Yyg0e%2BlWhcbhnDx30uUzV8qdK6YbqOaKAbPBE6ugE0B1cNKboOkw3ef4PW4kqfbnmAAKJgZoYjTQ4NIxXrqvlWmbdELccivLFAMUrci87OQ3v%2BihLiOnZAW27uKn9BmjNBbmIXEKRlHqlbwFwdgX9K8k%2B%2FoakctGkpxotsIXUUCXPUZ0ackZjwN8k%2BtJVqnxZB%2Fo9A0iD2evJrH3X3Hme9xtk1WqvV6R0bqKQEJSVAXDWwIJKHgdDONVuqvLumuO4XZY9hC6%2Bz7UteJGlHms3IvQdm7FMH5Eg%2Fru0XFnnETRjaE7daGzeR3pYL9i0GSSY1yH6zuiLOXu1knMgxyfPm49D6f8mb3FJmAviDOMvka7YDhCNtexoWQW%2BJyqVTCsEXhqy1bhFccSM9n7JickmS2YUgfIGTld4QTzGvOgJHPkgrIiIX6uWFWt%2Bdhm3TgaVmOQXQdi341X5cFAWGerAvJTQZ0tx7TfcP7406y2e%2Fuybg5kfeJt19nL3z%2BvggQ%2FbtzstZ8ZENZFQ8A%2F38C2r6v4Z8M5Ms2B9MnmTmelaAlk1xSqwsoxXFPuFXR1Td9VzWkg9XA6uZXHSlUr6s6OxLIONYg6%2Fpi7IvQ7qz9Uq848t1mkLWFTrvOYggG0vsXIhsq0YJhtw6hfYsTKtCO7o7W65yc3cb%2B4kbu8mspnz4QFggIzbUM2CpYpGwWe1xgYUS43PzFawOgoOhNOZhOg1u4pX0y0X%2FWpTatu03TTGTnhpOK5IvNijmIAbixLrC%2FBvhmnhjvZSMht6QgJ8tmvkNVI2Y%2BER7%2FoXeCbxSPoUyQ8HEvi6R9E4uVp8x1S6cDi%2FaOnyHcATa76nWOOyBRoBmTv5Yg0MyWaybZQslii1WtKAsgmiINPGERPYoKmKcnQ1f7IRK%2BQVFV75mN9bQtyhPK3aFpyuyiZlg5XAmVT7REiFYiupONnII%2BmiFb3NubBni5ND%2FWWrUgt3LzmZr1%2BOI6QVtdUR4HjR2zWsxS4%2FV5Tka15K8BXR%2BxbLVFbNSJUEHQ0dnkdmF2wAZgnq5ICW16B85G3GIar9skKQuUDNmQb9y7x%2B0oDL81KfiHsLEbYa15y1zs6EldoGHxB1IlWaLQvkbosja%2F2VjnlepEfk0ppZzTo79KzDdE4pY4bXQgwdUeIHIdKdDXdaLQ8V0nY6soTEa8%2BXNrBXFpDV8DVEV1aq5dLWwEA3RYWaWjwva4bHgqjrC47FADrdIV63m12FcZqWK4Ob1OkmgYS05GDrQOCmngnu3rBTmeMbYfs9qWuF5Mnq0CogwQITX3Zkf2obhjVO9hqzMUKcqWDbqnYtztSUOhzQ4Y9sgQrVkjlflOcPniYke2jdNg51NOAyEC5EAb4pNFrh8BqX77w3pOBlchORCXNGXj9bZNPNHofQw%2Fuy%2B0ADy8e3rQuiVvhF3eZ6e%2BmQrditELKN3YKL2wPe56KwXPb0A1tMAUUujugAWUFPFqjMOCrWs%2B6eq8LNEO83foklWi7HbMO3z7EQOLaOvu6tg9pcpn2Bn%2Bny2LbXRZwh2GwifGbIcl3sb1QQpLp2Xi%2BGEi9DBOK7qMpqRdSqBdvbx2YonJd4PyaY3RbcoRP2VCk6Ks7PxQ3xTVnjryv39FIBrmP2zsyrgK42KsKrxy%2BeUK%2Fvb6gGT3huyFaNXRxmz%2FBhRpzlz9H7lf7TJbvzzaIey4g2fU6mm0oJKWrta0GikXwwFTnMXETLBNU0IZqZLCoLTzrS1AhAOfH%2B%2Bg5Sp1Va6%2Bzvq%2F0YBmnyXjWFOsNhHNQU%2BFcT1DrwdUurbJtWpJx7sR6Ygg5TVDuO34STQGyn79JC7q8movuvlYPm5IX23X4OVjgMMg0pJrACIvhN2QwPOawuewUWh6HobuJg3l212yROfa2NGbS9g3HuUalo2SbpHealjvXazKbZ7MZnE5VbJ5Oh2Mzkjd7VmwNCnXFlj423xV6WD4rtiCXmFlhQZExZ8PG%2FcT4JPB0h1tVrjZTtIkHnSjdJLONxC9b6gviA6BQMCRTHjhjpIIhb0Wvapjbqkkz6q47eE7nQm%2B7jmhA1WLlKFqsgLg59TD01uH1SLpQFFaBDFCYEn0UU6LLELPd%2BMzYTl1vhf%2F291aXTJ1drPDJDYNlWmgK8SIN9mOmFkG0%2FJwdFUWJ0g2bh8R7qrl1tXbkMrr9MeFRa%2B8Qsj0S5DJ5GLd7xouSlPCtv%2FYsjnlrFbWUPuE%2Bo9VQS%2Ft5DaoO6h4FUu3DYsiqboyj6qp4Ne9TTrNtNQ6YP7eEf3GTe2t3bAJAm5fdl7yvmdEoZtrP1DdzO1PQKNuf8ys39D9fEZbofcSpL5ZAQQxFGz4UlvDqoXCfEbGkQ3B5oVhiKBxkldswym6jUJf948slfD1xawK5UqAi%2FDiLEYBsPTflrwigOxZab8Nk%2F2%2BCXEDoP5sNF%2FqbQm4elAsDa5ZaYartcWy1cUnA1tANXVzDqif1LdU2ykcLhwy53luoV65ZVx0QcaE8jN09nRirrqKWLnSVDuXDaEh5n%2FF8GCC75X5lsTm667LT2QXg%2BZCpXBPWo9o0lSu3kTQA%2BigJReOSot6GEjnX1HpqUUcKyiu2mRsJ0GXt8lbYe%2FpG5kG0IsT%2FJ%2FUxja8b17sFp%2BM0Rgi4XrZ7jYzr%2FD7j4brR4RcZLkXj67Q1jW6h6UgaL4emeUY7taNzN77upDaXynPQdVOsjo%2Bk6%2Fw%2B4%2Bm64kc6eQ50Q7H4Q8xMYiJxrF8GluJlEDNMyJKwj6%2FrOQlx3JKHLYdVj8KGaw%2FdjFkpjgSeG94VGjEnSULWag1o2BX6GDLPy%2Bhv7qQZSQ2QkI%2B9scUidoMiDCOrY5UBKE62NOqa%2B3%2BjGJENTruzajvfpf1zefSRrUzgmx1NqhsdOcYwEtYuKC0S1i6aR5Mw8Rqj7ocHJQnjtlcSg%2FJHFdiEOCEU7w9PR%2FJ69vSAAV3sAdOg3GVvHi0Dj2ST87B2g%2FSkR0x%2FKXIFuy47TnQvJFv%2FbAluCNsAqTaWPd7iNIgUW8u6se%2F%2BZGPf8fzdkxsI8m04qsW4lkxuqwWeelSYZHIzwMocHIUD39cpOUFFWJFMPaVTYoir6Q3QrUo8TCFXDpYzVn%2FDHqF%2BERIfzYUAJ3AhWuThJC6EJA86TxMdw4XgGYTjaP%2FxQxJeP3%2B79o9TXUHiEkJdTM6Mqf28Leo8tP8UAUSLPOzR%2FiPJAzKPqP2mrP08rFyh3Zk8AP2DueJpkUEMSPk5eH1wtsVRBbox%2FrnFUbZ%2Fnh%2B4S%2Bqu9yda5GeYU8W09PUaAuZiwWyjo1gsdKr6iMUQT1h6TLSyvLGoL%2BvA98OmyGsA5xIhceEeVMROyhwxGqUaZ8qAc04C9p3Qp3SToA%2BxahWrG8h3u9vJFTitXMnd9L8Fye%2Fbec7tVZJs4uzjjP1dBslqO7%2F1mKWCs%2F%2BstiQM2IcHl0ZBtMwL%2FtnPTF4Qx5AmNFoplp0YuqI4DIw3s4sdUpKq1c4spbtvfSE%2BTmf8Hw%3D%3D)

# Tips
###https://www.google.com/finance

### How to get jar: mvn clean compile assembly:single


https://zhuoli.github.io/EarningManagement/

### How to keep java application running after logout
 1. screen
 2. launch java application
 3. Ctrl+A, Ctrl+D
 4. log out
 ...
 To resume: screen -r
