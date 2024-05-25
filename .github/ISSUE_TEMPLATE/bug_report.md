name: Bug report
about: Tell about a bug or issue you may have identified
title: Provide a general summary of the issue
labels: bug
assignees: ''
body:
  - type: input
    id: core-version
    attributes:
      label: What version of Bukkit are you using?
      placeholder: "e.g. 3871-Spigot-d2eba2c-3f9263b"
    validations:
      required: true
  - type: input
    id: version
    attributes:
      label: What version of Bukkit are you using?
      placeholder: "e.g. ClickableLinks v2.0.3"
    validations:
      required: true
  - type: textarea
    id: what-happened
    attributes:
      label: Describe the issue
      description: Provide a summary of the issue and what you expected to happen, including specific (minimal) steps to reproduce. For example, a list of other plugins and their settings that are relevant to the issue.
    validations:
      required: true