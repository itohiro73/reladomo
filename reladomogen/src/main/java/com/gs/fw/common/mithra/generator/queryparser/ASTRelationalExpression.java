/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/* Generated By:JJTree: Do not edit this line. ASTRelationalExpression.java */


package com.gs.fw.common.mithra.generator.queryparser;

import com.gs.fw.common.mithra.generator.AbstractAttribute;
import com.gs.fw.common.mithra.generator.MithraObjectTypeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ASTRelationalExpression extends SimpleNode implements LeafLevelExpression
{

    private ASTAttributeName left;
    private SimpleNode right;
    private Operator operator;

    public ASTRelationalExpression(int id) {
        super(id);
    }

    public ASTRelationalExpression(MithraQL p, int id) {
        super(p, id);
    }

    /**
     * this is for auto join. operation is always equality
     * @param left belongs to "this"
     * @param right other attribute
     * @param belongsToThis flag to check
     */
    public ASTRelationalExpression(AbstractAttribute left, AbstractAttribute right, boolean belongsToThis)
    {
        super(0);
        this.left = new ASTAttributeName(left, belongsToThis);
        this.jjtAddChild(this.left, 0);
        ASTRelationalOperator equality = new ASTRelationalOperator(0);
        equality.setEqual(true);
        this.jjtAddChild(equality, 1);
        this.operator = equality;
        this.right = new ASTAttributeName(right, false);
        this.jjtAddChild(this.right, 2);
    }

    public ASTRelationalExpression(AbstractAttribute left, SimpleNode right, boolean belongsToThis)
    {
        super(0);
        this.left = new ASTAttributeName(left, belongsToThis);
        this.jjtAddChild(this.left, 0);
        ASTRelationalOperator equality = new ASTRelationalOperator(0);
        equality.setEqual(true);
        this.jjtAddChild(equality, 1);
        this.operator = equality;
        this.right = right;
        this.jjtAddChild(this.right, 2);
    }

    public Operator getOperator()
    {
        return this.operator;
    }

    public void checkConsistency(MithraObjectTypeWrapper owner, Map allObjects, List<String> errors)
    {
        if (this.children.length < 3)
        {
            this.operator = (Operator) this.children[1];
            if (!this.operator.isUnary())
            {
                errors.add("Relational expression has too few arguments");
            }
            this.left = (ASTAttributeName) this.children[0];
            return;
        }
        if (this.children[0] instanceof ASTLiteral && this.children[2] instanceof ASTLiteral)
        {
            errors.add("can't compare two literals!");
            return;
        }
        if (this.children[0] instanceof ASTLiteral)
        {
            this.left = (ASTAttributeName) this.children[2];
            this.right = (SimpleNode) this.children[0];
            this.operator = ((Operator) this.children[1]).getReverseOperator();
        }
        else
        {
            this.left = (ASTAttributeName) this.children[0];
            this.right = (SimpleNode) this.children[2];
            this.operator = (Operator) this.children[1];
        }
        if (this.left.findAttribute(owner, allObjects) != null) return; // can't do any more checking because the attribute is bogus
        if (this.right instanceof ASTAttributeName)
        {
            ASTAttributeName other = (ASTAttributeName)this.right;
            if (other.findAttribute(owner, allObjects) != null) return; // can't do any more checking because the attribute is bogus
            if (!left.isComparableTo(other))
            {
                errors.add("Attribute type mismatch: can't compare "+this.left.getName()+" and "+other.getName());
            }
        }
        else
        {
            if(this.right instanceof ASTLiteral)
			{
				ASTLiteral other = (ASTLiteral) this.right;
				if (!this.left.isComparableTo(other))
				{
					errors.add("Constant type mismatch: can't compare "+this.left.getName()+" and "+other.getValue());
				}
			}
        }
        if (!this.left.belongsToThis() && this.right instanceof ASTAttributeName) // we always want "this" on the left
        {
            SimpleNode temp = this.left;
            this.left = (ASTAttributeName) this.right;
            this.right = temp;
            this.operator = this.operator.getReverseOperator();
        }
        if (!this.isJoin() && this.operator.isIsNullOrIsNotNull())
        {
            if (!this.left.getAttribute().isNullable())
            {
                errors.add("'is null' is not valid for a non-nullable attribute: "+this.left.getAttribute().getOwner().getClassName()+"."+this.left.getAttribute().getName());
            }
        }
    }

    public void reAlignForOwner(MithraObjectTypeWrapper owner)
    {
        if (this.isJoin())
        {
            if (!this.left.getOwner().equals(owner))
            {
                SimpleNode temp = this.left;
                this.left = (ASTAttributeName) this.right;
                this.right = temp;
                this.operator = this.operator.getReverseOperator();
            }
        }
    }

    public boolean isJoin()
    {
        return this.right != null && this.right instanceof ASTAttributeName;
    }

    public boolean involvesThis()
    {
        return this.left.belongsToThis();
    }

    public boolean involvesClassAsNonThis(MithraObjectTypeWrapper owner)
    {
        if (!this.left.belongsToThis() && this.left.getOwner().equals(owner)) return true;
        return (isJoin() && ((ASTAttributeName)this.right).getOwner().equals(owner));
    }

    public boolean involvesOnlyThis(MithraObjectTypeWrapper mithraObjectTypeWrapper)
    {
        return !this.isJoin() && this.involvesClassAsNonThis(mithraObjectTypeWrapper);
    }

    public ASTAttributeName getLeft()
    {
        return this.left;
    }

    public SimpleNode getRight()
    {
        return this.right;
    }

    public List getEqualityRelationalExpressions(MithraObjectTypeWrapper from, MithraObjectTypeWrapper to)
    {
        ArrayList result = new ArrayList(1);
        if (this.operator.isEqual() && this.isJoin())
        {
            if (this.left.getOwner().equals(from) && ((ASTAttributeName)this.right).getOwner().equals(to))
            {
                result.add(this);
            }
        }
        return result;
    }

    public String getTopLevelFinderExpression()
    {
        return this.left.getTopLevelFinderAttribute()+"."+this.operator.getMethodName()+"("+
                this.right.getFinderString()+")";
    }

    public String getFinderExpressionForAlias(String alias)
    {
        return this.left.getOwner().getClassName()+"Finder."+left.getAttribute().getName()+"()."+
                this.operator.getMethodName()+"("+
                this.right.getFinderString()+")";
    }

    public void addLeafLevelExpressionsToList(List list)
    {
        list.add(this);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(MithraQLVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

	public String toString()
	{
		return getTopLevelFinderExpression();
	}

    public boolean equalsOther(SimpleNode simpleNode)
    {
        boolean result = false;
        if (simpleNode instanceof ASTRelationalExpression)
        {
            ASTRelationalExpression other = (ASTRelationalExpression) simpleNode;
            result = this.left.equalsOther(other.left);
            if (result)
            {
                result = this.operator.equalsOther((SimpleNode) other.operator);
            }
            if (result && this.right != null)
            {
                result = this.right.equalsOther(other.right);
            }
        }
        return result;
    }

    public String getJoinExpression()
    {
        AbstractAttribute rightAttribute = ((ASTAttributeName)this.right).getAttribute();
        return this.left.getAttribute().getOwner().getFinderClassName()+"."+this.left.getAttribute().getName()+
                "().constructEqualityMapper("+rightAttribute.getOwner().getFinderClassName()+"."+rightAttribute.getName()+"())";
    }

    public boolean isRightHandJavaLiteral()
    {
        SimpleNode literal = this.getRight();
        return literal != null && literal instanceof ASTLiteral && ((ASTLiteral)literal).isJavaLiteral();
    }

    public String getConstantExpression(MithraObjectTypeWrapper fromObject)
    {
        String result = this.getLeft().getOwner().getFinderClassName()+"."+this.getLeft().getAttribute().getName()+"()."+
                this.getOperator().getMethodName()+"(";
        result += getLiteralRightHand(fromObject);
        result += ")";
        return result;
    }

    public String getLiteralRightHand(MithraObjectTypeWrapper fromObject)
    {
        SimpleNode literal = this.getRight();
        String toAppend = "";
        if (literal != null)
        {
            if (literal instanceof ASTInLiteral)
            {
                ASTInLiteral inLiteral = (ASTInLiteral) literal;
                if (inLiteral.isJavaLiteral())
                {
                    inLiteral.setFinderString(inLiteral.getValue());
                    toAppend += inLiteral.getValue();
                }
                else
                {
                    toAppend += this.getLeft().getArgumentForInOperation(inLiteral.getValue(), fromObject);
                }
            }
            else
            {
                toAppend += getNonInLiteral(literal);
            }
        }
        return toAppend;
    }

    public String getNonInLiteral(SimpleNode literal)
    {
        ASTLiteral astLiteral = (ASTLiteral) literal;
        String result;
        if (astLiteral.isJavaLiteral())
        {
            result = astLiteral.getValue();
        }
        else
        {
            result = this.getLeft().getAttribute().parseLiteralAndCast(astLiteral.getValue());
        }
        return result;
    }

    @Override
    public String constructFilterExpr()
    {
        AbstractAttribute attribute = this.left.getAttribute();
        String prefix = "_data.";
        if (attribute.isAsOfAttribute() || (attribute.getOwner().isReadOnly() && !attribute.getOwner().hasAsOfAttributes()))
        {
            prefix = "this.";
        }
        if (this.left.belongsToThis())
        {
            if (this.operator.isUnary())
            {
                if (attribute.isNullable() && this.operator.isIsNullOrIsNotNull())
                {
                    String not = ((ASTIsNullClause) this.operator).isNot() ? "!" : "";
                    return not + prefix + attribute.getNullGetter();
                }
            }
            else if (this.right instanceof ASTLiteral || this.right instanceof ASTInLiteral)
            {
                String expressionStr = prefix + attribute.getGetter() + "()";
                String literal = this.getLiteralRightHand(attribute.getOwner());
                if (this.operator.isIn())
                {
                    expressionStr = literal+".contains("+expressionStr+")";
                }
                else
                {
                    if (attribute.isPrimitive())
                    {
                        expressionStr += ("=".equals(this.operator.toString()) ? "==" : this.operator.toString()) + literal;
                    }
                    else
                    {
                        if (this.operator.isEqual())
                        {
                            expressionStr += ".equals(" + literal + ")";
                        }
                        else if (this.operator.isNotEqual())
                        {
                            expressionStr = "!" + expressionStr + ".equals(" + literal + ")";
                        }
                        else
                        {
                            expressionStr += ".compareTo(" + literal + ")";
                            expressionStr += this.operator.toString() + " 0";
                        }
                    }
                }
                if (attribute.isNullable())
                {
                    expressionStr = "!" + prefix + attribute.getNullGetter() + " && " + expressionStr;
                }
                return expressionStr;
            }
            else if (attribute.isNullable())
            {
                return "!" + prefix + attribute.getNullGetter();
            }
        }
        return "";
    }
}
