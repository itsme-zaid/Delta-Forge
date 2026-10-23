## base = {"a": 1}
## ours = {"a": null}   // user set a to null
## theirs = {}          // user deleted a

## merge(base, ours, theirs):
<p>- If null == "missing": both "deleted" a? No — ours changed a to null, theirs deleted.
  That's a DELETE_VS_EDIT conflict.
- If null != "missing": ours changed a to null (edit), theirs deleted a (delete).
  Same conflict, but the CONFLICT REPORT shows ours=null (present), theirs=missing.
  That's the correct report.</p>