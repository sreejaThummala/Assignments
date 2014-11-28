#define DEBUG_TYPE "optLoads"
#include "llvm/Pass.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Instruction.h"
#include "llvm/ADT/StringExtras.h"
#include "llvm/ADT/Statistic.h"
#include "llvm/Support/InstIterator.h"
#include <iostream>
#include <vector>
#include "llvm/ADT/DenseMap.h"
using namespace llvm;

namespace {
  class optLoads : public FunctionPass {
    DenseMap<const Value*, int> instMap;
    public:
    static char ID; // Pass identification, replacement for typeid
    optLoads() : FunctionPass(ID) {}

    //**********************************************************************
    // runOnFunction
    //**********************************************************************
    virtual bool runOnFunction(Function &F) {
      // print fn name
     // std::cerr << "FUNCTION " << F.getName().str() << "\n";

      // MISSING: Add code here to do the following:
      //          1. Iterate over the instructions in F, creating a
      //             map from instruction address to unique integer.
      //             (It is probably a good idea to put this code in
      //             a separate, private method.)
      //          2. Iterate over the basic blocks in the function and
      //             print each instruction in that block using the format
      //             given in the assignment.
        addToMap(F);
        bool changed = false;


        for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
            for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i)
            {
                     if (isa<StoreInst>(&*i)) {
                             Value *v = i->getOperand(0), *m = i->getOperand(1);
                             ++i;
                             BasicBlock::iterator k = i;
                             if (isa<LoadInst>(&*k)) {
                                     ++i;
                                      if (m == k->getOperand(0)) {
                                                std::cerr << "%" << instMap.lookup(&*k) << " is a useless load\n";
                                                 k->replaceAllUsesWith(v);
                                                 k->eraseFromParent();
                                                 changed = true;
                                      }
                             } else {
                                     --i;
                             }

                     } //else
                         //    ++i;
            }

    }

      return changed;  // because we have NOT changed this function
    }

    void addToMap(Function &F) {
          static int id = 1;
for (inst_iterator i = inst_begin(F), E = inst_end(F); i != E; ++i, ++id)
                                        // Convert the iterator to a pointer, and insert the pair
     instMap.insert(std::make_pair(&*i, id));
                                             	}
                                        //

    // print (do not change this method)
    //
    // If this pass is run with -f -analyze, this method will be called
    // after each call to runOnFunction.
    //**********************************************************************
    virtual void print(std::ostream &O, const Module *M) const {
        O << "This is optLoads.\n";
    }

    //**********************************************************************
    // getAnalysisUsage
    //**********************************************************************

    // We don't modify the program, so we preserve all analyses
    virtual void getAnalysisUsage(AnalysisUsage &AU) const {
      AU.setPreservesAll();
    };
  };
  char optLoads::ID = 0;

  // register the printCode class:
  //  - give it a command-line argument (printCode)
  //  - a name ("print code")
  //  - a flag saying that we don't modify the CFG
  //  - a flag saying this is not an analysis pass
  RegisterPass<optLoads> X("optLoads", "optimize unnecessary loads",
                           false, false);
}
