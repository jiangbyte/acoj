/*
  Drop oj_problem.allowed_languages — languages are derived from oj_problem_language_limit.
  Use when the previous language_limit alter already dropped the four limit columns.
*/

SET NAMES utf8mb4;

ALTER TABLE `oj_problem`
DROP
COLUMN `allowed_languages`;
