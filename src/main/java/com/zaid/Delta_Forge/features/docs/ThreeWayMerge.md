
## 3 way merge: 
- let say we are merging 2 branches "ours" and "theirs". Now to merge successfully we need a source of truth / reference. That would be the common ancestor of both the branches i.e <B> the latest document before splitting of the branch</b>
  - The common 3 way merge :
    - if ours == base -> we pick theirs
    - if theirs == base -> we pick ours
    - if ours == theirs -> we pick ours/theirs (any)
    - else there's a conflict (report)
  
  
    Why 3 way merge, why not just compare the two of the branches document:
    - if we dont have a base, we can only compare ours == theirs (successful merge) and 
        ours!=theirs (conflict report)
    - what we lose is if only one branch was changed and the other branch wasnt changed at
        all we would still be reporting conflict.  